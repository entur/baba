package no.rutebanken.baba.organisation.m2m.support;

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.Collection;
import java.util.stream.Stream;

public class M2MUtils {

    private M2MUtils() {}

    public static final String CLIENT_SUBJECT_SUFFIX = "@clients";

    /**
     * Extract the OAuth2 client-id from the subject claim.
     * The subject claim structure for m2m clients is "client-id@clients"
     */
    public static String clientId(String subject) {
        return subject.replace(CLIENT_SUBJECT_SUFFIX, "");
    }

    /**
     * Extract the role assignments from a given M2M client.
     */
    public static Stream<RoleAssignment> toRoleAssignments(M2MClient client) {
        return client.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment);
    }



    public static void validateM2MClient(AuthenticatedUser authenticatedUser) {
        if (!authenticatedUser.isClient()) {
            throw new IllegalArgumentException("The user is not a machine-to-machine client: " + authenticatedUser.subject());
        }
    }


    public static  Stream<RoleAssignment> getRoleAssignmentsFromDatabase(AuthenticatedUser authenticatedUser, M2MClientRepository repository) {
        M2MClient client = repository.getOneByPublicIdIfExists(clientId(authenticatedUser.subject()));
        if (client == null) {
            return Stream.empty();
        }
        if (authenticatedUser.organisationId() != client.getEnturOrganisationId()) {
            throw new IllegalArgumentException("Organisation id mismatch: expected " + client.getEnturOrganisationId() + ", but was " + authenticatedUser.organisationId());
        }
        if ((authenticatedUser.isInternal() && !client.isInternal()) || (authenticatedUser.isPartner() && !client.isPartner())) {
            throw new IllegalArgumentException("Issuer mismatch: expected " + client.getIssuer());
        }
        return toRoleAssignments(client);
    }
}
