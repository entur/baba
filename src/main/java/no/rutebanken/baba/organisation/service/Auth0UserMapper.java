package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static no.rutebanken.baba.organisation.service.IamUtils.generatePassword;
import static no.rutebanken.baba.organisation.service.IamUtils.toAtr;

@Service
public class Auth0UserMapper {

    private static final String ROR_ROLES = "ror_roles";
    private static final String ROR_CREATED_BY_ROR = "ror_created_by_ror";

    private final String connection;
    private final String preProvisionedConnection;

    public Auth0UserMapper(@Value("${iam.auth0.admin.connection:Username-Password-Authentication}") String connection,
                           @Value("${iam.auth0.admin.connection.preprovisioning:preprovision}") String preProvisionedConnection) {
        this.connection = connection;
        this.preProvisionedConnection = preProvisionedConnection;
    }

    public com.auth0.json.mgmt.users.User mapToNewPrimaryAuth0User(User user) {
        return toAuth0User(user, null, false, false);

    }

    public com.auth0.json.mgmt.users.User mapToUpdatedPrimaryAuth0User(User user, com.auth0.json.mgmt.users.User existingAuth0User) {
        Objects.requireNonNull(existingAuth0User);
        return toAuth0User(user, existingAuth0User, false, false);

    }


    public com.auth0.json.mgmt.users.User mapToUpdatedFederatedAuth0User(User user, com.auth0.json.mgmt.users.User existingAuth0User) {
        Objects.requireNonNull(existingAuth0User);
        return toAuth0User(user, existingAuth0User, true, false);
    }


    public com.auth0.json.mgmt.users.User mapToPreProvisionedFederatedAuth0User(User user) {
        return toAuth0User(user, null, true, true);
    }

    public com.auth0.json.mgmt.users.User mapToAlreadyPreProvisionedFederatedAuth0User(User user, com.auth0.json.mgmt.users.User existingAuth0User) {
        return toAuth0User(user, existingAuth0User, true, true);
    }

    /**
     * Return an Auth0 user with an empty key for roles metadata.
     * This will force the deletion of this key in the user metadata.
     */
    public com.auth0.json.mgmt.users.User mapForDeletion() {
        com.auth0.json.mgmt.users.User deletedUser = new com.auth0.json.mgmt.users.User();
        deletedUser.setAppMetadata(Map.of(ROR_ROLES, List.of()));
        return deletedUser;

    }


    private com.auth0.json.mgmt.users.User toAuth0User(User user, com.auth0.json.mgmt.users.User existingAuth0User, boolean isFederated, boolean isPreProvisioned) {

        com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();

        // the Auth0 API will not overwrite the existing metadata, it will merge it,
        // thus it is not necessary to retrieve the existing metadata.
        Map<String, Object> attributes = new HashMap<>();
        if (existingAuth0User == null) {
            auth0User.setPassword(generatePassword().toCharArray());
            if (!isFederated) {
                attributes.put(ROR_CREATED_BY_ROR, "true");
             }
        }

        if (!isFederated) {
            auth0User.setConnection(connection);
            // The Auth0 API refuses to update both the username and the email at the same time
            // we set the username only when creating a new primary user.
            if(existingAuth0User == null) {
                auth0User.setUsername(user.getUsername());
            }

            // set contact details only if the user was created by RoR
            if (existingAuth0User == null  || isCreatedByRoR(existingAuth0User)) {
                auth0User.setGivenName(user.getContactDetails().getFirstName());
                auth0User.setFamilyName(user.getContactDetails().getLastName());
                auth0User.setEmail(user.getContactDetails().getEmail());
                auth0User.setName(auth0User.getGivenName() + ' ' + auth0User.getFamilyName());
            }
        }

        if (isPreProvisioned) {
            auth0User.setConnection(preProvisionedConnection);
            auth0User.setEmail(user.getContactDetails().getEmail());
        }

        if (user.getResponsibilitySets() != null) {
            List<String> roleAssignments = new ArrayList<>();

            for (ResponsibilitySet responsibilitySet : user.getResponsibilitySets()) {
                if (responsibilitySet.getRoles() != null) {
                    responsibilitySet.getRoles().forEach(rra -> roleAssignments.add(toAtr(rra)));
                }
            }
            attributes.put(ROR_ROLES, roleAssignments);
            auth0User.setAppMetadata(attributes);
        }

        return auth0User;
    }


    /**
     * Return true if the Auth0 user account was created by RoR.
     * This excludes primary users that were created directly in Entur Partner and federated users.
     */
    private static boolean isCreatedByRoR(com.auth0.json.mgmt.users.User existingAuth0User) {
        return "true".equals(existingAuth0User.getAppMetadata().get(ROR_CREATED_BY_ROR));
    }

}
