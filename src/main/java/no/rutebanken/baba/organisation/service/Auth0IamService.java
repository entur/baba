package no.rutebanken.baba.organisation.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import no.rutebanken.baba.organisation.model.OrganisationException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.RoleRepository;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.permissionstore.service.PermissionStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("auth0")
public class Auth0IamService implements IamService {

    private static final String ROR_ROLES_PREFIX = "ror_";
    public static final int HTTP_STATUS_CODE_409_CONFLICT = 409;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> defaultRoles;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Auth0ManagementApi auth0ManagementAPI;
    private final Auth0UserMapper auth0UserMapper;
    private final PermissionStoreService permissionStoreService;

    public Auth0IamService(UserRepository userRepository,
                           RoleRepository roleRepository,
                           AuthAPI authAPI,
                           Auth0UserMapper auth0UserMapper,
                           PermissionStoreService permissionStoreService,
                           @Value("#{'${iam.auth0.default.roles:rutebanken}'.split(',')}") List<String> defaultRoles,
                           @Value("${iam.auth0.admin.domain}") String domain) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.defaultRoles = defaultRoles;
        this.auth0ManagementAPI = new Auth0ManagementApi(authAPI, domain);
        this.auth0UserMapper = auth0UserMapper;
        this.permissionStoreService = permissionStoreService;
    }

    /**
     * This implementation handles differently "primary users" (those stored directly in a user-password database in the Auth0 tenant)
     * and "federated users" (that is: secondary users that are managed by an external system).
     * If the user account is federated:
     *  <ul>
     *      <li>if the user account exists already (that is: the user has logged in at least once in the tenant), its metadata and roles are updated.</li>
     *      <li>if the user account does not exist yet, its metadata and roles are updated in the pre-provisioning database
     *      (and the metadata and roles will be copied over from the pre-provisioning database when the user logs in for the first time).</li>
     * </ul>
     *
     * <br>If the user account is a primary account:
     * <ul>
     *  <li>if the user account exists already in the tenant, its metadata and roles are updated.</li>
     *  <li>if the user account does not exist yet in the tenant, the account is created, its metadata and roles are updated.</li>
     * </ul>
     **/
    @Override
    public boolean createOrUpdate(User user) {
        return switch (getAuth0AccountStatus(user)) {
            case FEDERATED_ACCOUNT_PRESENT -> {
                updateFederatedUser(user);
                yield false;
            }
            case FEDERATED_ACCOUNT_MISSING_ALREADY_PREPROVISIONED -> {
                updatePreProvisionedFederatedUser(user);
                yield false;
            }
            case FEDERATED_ACCOUNT_MISSING_NOT_PREPROVISIONED -> {
                preProvisionFederatedUser(user);
                yield true;
            }
            case PRIMARY_ACCOUNT_PRESENT -> {
                updatePrimaryUser(user);
                yield false;
            }
            case PRIMARY_ACCOUNT_MISSING -> {
                createPrimaryUser(user);
                yield true;
            }
        };
    }

    /**
     * This implementation removes RoR roles and metadata, but does not delete the account in Auth0.
     */
    @Override
    public void removeUser(User user) {
        logger.info("Removing user {} from Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User existingAuth0User;
        try {
            existingAuth0User = getAuth0UserByEmail(user.getContactDetails().getEmail());
        } catch (OAuth2UserNotFoundException nfe) {
            logger.warn("Ignoring user removal for user {} that does not exist in the Auth0 tenant", user.getUsername());
            return;
        }
        removeUser(user, existingAuth0User);
    }

    private void removePreProvisionedUser(User user) {
        logger.info("Removing pre-provisioned user {} from Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User existingAuth0User;
        try {
            existingAuth0User = getAuth0UserByEmail(user.getContactDetails().getEmail(), true);
        } catch (OAuth2UserNotFoundException nfe) {
            logger.warn("Ignoring user removal for pre-provisioned user {} that does not exist in the Auth0 tenant", user.getUsername());
            return;
        }
        removeUser(user, existingAuth0User);
    }

    private void removeUser(User user, com.auth0.json.mgmt.users.User existingAuth0User) {
        // delete RoR metadata
        try {
            com.auth0.json.mgmt.users.User updatedAuth0User = auth0UserMapper.mapForDeletion();
            auth0ManagementAPI.getManagementAPI().users().update(existingAuth0User.getId(), updatedAuth0User).execute();
            logger.info("RoR metadata for user {} successfully removed from Auth0", user.getUsername());
        } catch (Auth0Exception e) {
            logger.error("Failed to remove RoR metadata of user {} from Auth0", user.getUsername(), e);
            throw new OrganisationException("Failed to remove user metadata from Auth0");
        }
        // delete RoR roles.
        user.setResponsibilitySets(Set.of());
        updateRoles(user, existingAuth0User, getAllSystemRoles());
    }

    @Override
    public void createRole(Role role) {
        logger.info("Creating role {} in Auth0: ", role.getId());
        com.auth0.json.mgmt.roles.Role auth0Role = toAuth0Role(role);
        try {
            com.auth0.json.mgmt.roles.Role createdRole = auth0ManagementAPI.getManagementAPI().roles().create(auth0Role).execute().getBody();
            logger.info("Role {} successfully created in Auth0 with target name {}", role.getId(), createdRole.getName());
        } catch (APIException e) {
            if(e.getStatusCode() == HTTP_STATUS_CODE_409_CONFLICT) {
                logger.info("The role {} already exists", role.getId());
            }
            else {
                throw new OrganisationException("Failed to create role");
            }

        } catch (Exception e) {
            logger.error("Failed to create role {}", role.getId(), e);
            throw new OrganisationException("Failed to create role");
        }
    }

    @Override
    public void removeRole(Role role) {
        logger.info("Removing role in Auth0: {}", role.getId());
        com.auth0.json.mgmt.roles.Role auth0Role = getAuth0RoleByPrivateCode(role.getPrivateCode());
        try {
            auth0ManagementAPI.getManagementAPI().roles().delete(auth0Role.getId()).execute();
            logger.info("Role {} successfully removed from Auth0", role.getId());
        } catch (Exception e) {
            logger.error("Failed to remove role {} from Auth0", role.getId(), e);
            throw new OrganisationException("Failed to remove role from Auth0");
        }
    }

    @Override
    public void updateResponsibilitySet(ResponsibilitySet responsibilitySet) {
        logger.info("Updating responsibility set {} in Auth0", responsibilitySet.getId());
        List<Role> systemRoles = getAllSystemRoles();
        try {
            userRepository.findUsersWithResponsibilitySet(responsibilitySet).forEach(u -> {
                com.auth0.json.mgmt.users.User auth0User = getAuth0UserByEmail(u.getContactDetails().getEmail());
                updateRoles(u, auth0User, systemRoles);
            });
        } catch (Exception e) {
            logger.info("Failed to update responsibility set {} in Auth0", responsibilitySet.getId(), e);
            throw new OrganisationException("Failed to update responsibility set in Auth0");
        }
    }

    private Auth0AccountStatus getAuth0AccountStatus(User user) {
        if (isFederated(user.getContactDetails().getEmail())) {
            if (hasUser(user)) {
                return Auth0AccountStatus.FEDERATED_ACCOUNT_PRESENT;
            } else if (hasPreProvisionedUser(user)) {
                return Auth0AccountStatus.FEDERATED_ACCOUNT_MISSING_ALREADY_PREPROVISIONED;
            } else {
                return Auth0AccountStatus.FEDERATED_ACCOUNT_MISSING_NOT_PREPROVISIONED;
            }
        } else {
            if (hasUser(user)) {
                return Auth0AccountStatus.PRIMARY_ACCOUNT_PRESENT;
            } else {
                return Auth0AccountStatus.PRIMARY_ACCOUNT_MISSING;
            }
        }
    }

    /**
     * Return true if the email address belongs to a federated domain.
     */
    private boolean isFederated(String email) {
        Objects.requireNonNull(email);
        return permissionStoreService.isFederated(email);

    }

    /**
     * Return true if the user exists already in the Auth0 tenant, either as a primary user or a federated user.
     * Pre-provisioned user are excluded.
     */
    private boolean hasUser(User user) {
        try {
            getAuth0UserByEmail(user.getContactDetails().getEmail());
            return true;
        } catch (OAuth2UserNotFoundException nfe) {
            return false;
        }
    }

    /**
     * Return true if the federated user has already a pre-provisioned account.
     */
    private boolean hasPreProvisionedUser(User user) {
        try {
            getAuth0UserByEmail(user.getContactDetails().getEmail(), true);
            return true;
        } catch (OAuth2UserNotFoundException nfe) {
            return false;
        }
    }

    /**
     * Create a primary user in the local Auth0 user-password database.
     */
    private void createPrimaryUser(User user) {
        logger.info("Creating a primary user {} in Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User auth0User = auth0UserMapper.mapToNewPrimaryAuth0User(user);
        com.auth0.json.mgmt.users.User createdUser = null;
        try {
            createdUser = auth0ManagementAPI.getManagementAPI().users().create(auth0User).execute().getBody();
            logger.info("Created user {} with Auth0 id {}", user.getUsername(), createdUser.getId());
            updateRoles(user, createdUser, getAllSystemRoles());
        } catch (Exception e) {
            logger.error("User creation failed for user {}", user, e);
            if (createdUser != null) {
                logger.info("Attempting to remove user {}", user);
                removeUser(user);
            }
            throw new OrganisationException("User creation failed");
        }
    }


    /**
     * Update a primary user in the local Auth0 user-password database.
     * If the user was not created by RoR, only roles and metadata are updated.
     */
    private void updatePrimaryUser(User user) {
        logger.info("Updating primary user {} in Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User existingAuth0User = getAuth0UserByEmail(user.getContactDetails().getEmail());
        com.auth0.json.mgmt.users.User updatedAuth0User = auth0UserMapper.mapToUpdatedPrimaryAuth0User(user, existingAuth0User);
        try {
            auth0ManagementAPI.getManagementAPI().users().update(existingAuth0User.getId(), updatedAuth0User).execute();
            updateRoles(user, existingAuth0User, getAllSystemRoles());
            logger.info("User {} successfully updated in Auth0", user.getUsername());
        } catch (Exception e) {
            logger.error("User update in Auth0 failed for user {}", user.getUsername(), e);
            throw new OrganisationException("User update in Auth0 failed");
        }
    }

    /**
     * Create a new account in the pre-provisioning database.
     * Copy roles and metadata for the given federated user into the pre-provisioning database.
     * When the user logs in for the first time, Auth0 will automatically apply the roles and metadata to the user account.
     * Other user fields are ignored (email, name, ...)
     */
    private void preProvisionFederatedUser(User user) {
        logger.info("Creating a pre-provisioned federated user {} in Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User auth0User = auth0UserMapper.mapToPreProvisionedFederatedAuth0User(user);
        com.auth0.json.mgmt.users.User createdUser = null;
        try {
            createdUser = auth0ManagementAPI.getManagementAPI().users().create(auth0User).execute().getBody();
            logger.info("Created pre-provisioned user {} with Auth0 id {}", user.getUsername(), createdUser.getId());
            updateRoles(user, createdUser, getAllSystemRoles());
        } catch (Exception e) {
            logger.error("Pre-provisioned user creation failed for user {}", user, e);
            if (createdUser != null) {
                logger.info("Attempting to remove pre-provisioned user {}", user);
                removePreProvisionedUser(user);
            }
            throw new OrganisationException("Pre-provisioned user creation failed");
        }
    }

    /**
     * Update an existing account in the pre-provisioning database.
     */
    private void updatePreProvisionedFederatedUser(User user) {
        logger.info("Updating pre-provisioned user {} in Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User existingAuth0User = getAuth0UserByEmail(user.getContactDetails().getEmail(), true);
        com.auth0.json.mgmt.users.User updatedAuth0User = auth0UserMapper.mapToAlreadyPreProvisionedFederatedAuth0User(user, existingAuth0User);
        try {
            auth0ManagementAPI.getManagementAPI().users().update(existingAuth0User.getId(), updatedAuth0User).execute();
            updateRoles(user, existingAuth0User, getAllSystemRoles());
            logger.info("User {} successfully updated in Auth0", user.getUsername());

        } catch (Exception e) {
            logger.error("User update in Auth0 failed for user {}", user.getUsername(), e);
            throw new OrganisationException("User update in Auth0 failed");
        }

    }

    /**
     * Update roles and metadata for the given federated user.
     * Other fields are ignored (email, name, ...)
     */
    private void updateFederatedUser(User user) {
        logger.info("Updating federated user {} in Auth0", user.getUsername());
        com.auth0.json.mgmt.users.User existingAuth0User = getAuth0UserByEmail(user.getContactDetails().getEmail());
        com.auth0.json.mgmt.users.User updatedAuth0User = auth0UserMapper.mapToUpdatedFederatedAuth0User(user, existingAuth0User);
        try {
            auth0ManagementAPI.getManagementAPI().users().update(existingAuth0User.getId(), updatedAuth0User).execute();
            updateRoles(user, existingAuth0User, getAllSystemRoles());
            logger.info("User {} successfully updated in Auth0", user.getUsername());

        } catch (Exception e) {
            logger.error("User update in Auth0 failed for user {}", user.getUsername(), e);
            throw new OrganisationException("User update in Auth0 failed");
        }

    }

    /**
     * Return all roles defined in the role repository.
     */
    private List<Role> getAllSystemRoles() {
        return roleRepository.findAll();
    }


    /**
     * Update the user roles.
     * Only roles that are defined in the organisation repository are added or removed.
     * Roles that are assigned to the user in Auth0 but that are not defined in the organisation repository are ignored.
     *
     * @param user        the user in the organisation repository whose roles should be updated in Auth0.
     * @param auth0User   the user in Auth0.
     * @param systemRoles all the roles that are defined in the organisation repository.
     */
    private void updateRoles(User user, com.auth0.json.mgmt.users.User auth0User, List<Role> systemRoles) {
        logger.info("Updating roles for user {}", user.getUsername());
        // TODO prefix roles with ror_
        try {
            // all the role names defined in the organisation repository
            Set<String> systemRoleNames = systemRoles.stream()
                    .map(Role::getId)
                    .map(role -> ROR_ROLES_PREFIX + role)
                    .collect(Collectors.toSet());
            // the role names assigned to the user in the organisation repository
            Set<String> newRoleNames = getRoleNames(user).stream()
                    .map(role -> ROR_ROLES_PREFIX + role)
                    .collect(Collectors.toSet());
            // the Auth0 roles currently assigned to the Auth0 user
            List<com.auth0.json.mgmt.roles.Role> existingAuth0UserRoles = auth0ManagementAPI.getManagementAPI().users()
                    .listRoles(auth0User.getId(), null)
                    .execute()
                    .getBody()
                    .getItems();
            // Auth0 role ids that are currently assigned to the Auth0 user and that should be removed
            List<String> auth0RoleIdsToBeRemoved = existingAuth0UserRoles.stream().filter(r -> systemRoleNames.contains(r.getName()))
                    .filter(r -> !newRoleNames.remove(r.getName())).map(com.auth0.json.mgmt.roles.Role::getId).toList();

            if (!auth0RoleIdsToBeRemoved.isEmpty()) {
                auth0ManagementAPI.getManagementAPI().users().removeRoles(auth0User.getId(), auth0RoleIdsToBeRemoved).execute();
            }

            if (!newRoleNames.isEmpty()) {
                List<String> rolesToBeAdded = newRoleNames.stream().map(this::getAuth0RoleByPrivateCode).map(com.auth0.json.mgmt.roles.Role::getId).toList();
                auth0ManagementAPI.getManagementAPI().users().addRoles(auth0User.getId(), rolesToBeAdded).execute();
            }

        } catch (Auth0Exception e) {
            logger.error("Failed to update roles for user {}", user.getUsername(), e);
            throw new OrganisationException("Failed to update roles");
        }

    }

    private com.auth0.json.mgmt.roles.Role toAuth0Role(Role role) {
        com.auth0.json.mgmt.roles.Role auth0Role = new com.auth0.json.mgmt.roles.Role();
        auth0Role.setName(ROR_ROLES_PREFIX +  role.getId());
        auth0Role.setDescription(role.getName());
        return auth0Role;
    }

    private com.auth0.json.mgmt.users.User getAuth0UserByEmail(String email) {
        return getAuth0UserByEmail(email, false);
    }


    private com.auth0.json.mgmt.users.User getAuth0UserByEmail(String email, boolean preProvisioning) {
        String query;
        //TODO replace hardcoded preprovisioning
        if (preProvisioning) {
            query = "email:\"" + email + "\" AND identities.connection:\"preprovision\"";
        } else {
            query = "email:\"" + email + "\" AND NOT identities.connection:\"preprovision\"";
        }

        try {
            List<com.auth0.json.mgmt.users.User> matchingUsers = auth0ManagementAPI.getManagementAPI()
                    .users()
                    .list(new UserFilter().withQuery(query))
                    .execute()
                    .getBody()
                    .getItems();
            if (matchingUsers.isEmpty()) {
                throw new OAuth2UserNotFoundException("User not found in Auth0: " + email);
            } else if (matchingUsers.size() > 1) {
                logger.error("More than one user found in Auth0 tenant with email: {}", email);
                throw new OrganisationException("More than one user found with email: " + email);
            }
            return matchingUsers.getFirst();
        } catch (Auth0Exception e) {
            logger.error("Failed to retrieve the user {} in Auth0", email, e);
            throw new OrganisationException("Failed to retrieve the user");
        }
    }

    private com.auth0.json.mgmt.roles.Role getAuth0RoleByPrivateCode(String privateCode) {
        try {
            // filtering twice by private code since the Auth0 filter accept all names that contain the filter.
            List<com.auth0.json.mgmt.roles.Role> matchingRoles = auth0ManagementAPI.getManagementAPI().roles()
                    .list(new RolesFilter().withName(privateCode))
                    .execute().getBody()
                    .getItems()
                    .stream().filter(r -> privateCode.equals(r.getName())).toList();
            if (matchingRoles.isEmpty()) {
                logger.warn("Role not found: {}", privateCode);
                throw new OrganisationException("Role not found: " + privateCode);
            } else if (matchingRoles.size() > 1) {
                logger.error("More than one role found with private code: {}", privateCode);
                throw new OrganisationException("More than one role found with private code:" + privateCode);
            }
            return matchingRoles.getFirst();
        } catch (Auth0Exception e) {
            logger.error("Failed to retrieve the role {} in Auth0", privateCode, e);
            throw new OrganisationException("Failed to retrieve role in Auth0");
        }
    }

    private Set<String> getRoleNames(User user) {
        Set<String> roleNames = new HashSet<>(defaultRoles);
        for (ResponsibilitySet responsibilitySet : user.getResponsibilitySets()) {
            roleNames.addAll(responsibilitySet.getRoles().stream().map(r -> r.getTypeOfResponsibilityRole().getId()).collect(Collectors.toSet()));
        }
        return roleNames;
    }


    private enum Auth0AccountStatus {
        FEDERATED_ACCOUNT_PRESENT,
        FEDERATED_ACCOUNT_MISSING_ALREADY_PREPROVISIONED,
        FEDERATED_ACCOUNT_MISSING_NOT_PREPROVISIONED,
        PRIMARY_ACCOUNT_PRESENT,
        PRIMARY_ACCOUNT_MISSING
    }

}
