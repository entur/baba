package no.rutebanken.baba.security.permissionstore;

import com.google.common.collect.Streams;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Build role assignments for Entur Internal machine-to-machine tokens.
 * Role assignments are built from configuration (application.properties) and from the "permissions" claim in the JWT token.
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturInternalM2MRoleAssignmentRepository {

    static final String DEFAULT_ADMIN_ORG = "RB";
    static final String CLIENT_SUBJECT_SUFFIX = "@clients";

    private final M2MClientRepository repository;

    public EnturInternalM2MRoleAssignmentRepository(M2MClientRepository repository) {
        this.repository = repository;
    }

    /**
     * Extract RoleAssignments from configuration and from the permission claim.
     */
    public List<RoleAssignment> getRolesAssignments(AuthenticatedUser authenticatedUser) {
        validateM2MClient(authenticatedUser);
        Stream<RoleAssignment> rolesFromDatabase = getRoleAssignmentsFromDatabase(authenticatedUser);
        Stream<RoleAssignment> rolesFromPermissions = getRoleAssignmentsFromPermissions(authenticatedUser);
        return Streams.concat(rolesFromDatabase, rolesFromPermissions).toList();
    }

    private void validateM2MClient(AuthenticatedUser authenticatedUser) {
        if (!authenticatedUser.isClient()) {
            throw new IllegalArgumentException("The user is not a machine-to-machine client: " + authenticatedUser.subject());
        }
    }

    /**
     * Internal clients (from Entur Internal) owned by RoR contain cross-organization roles under the permission claim.
     */
    private static Stream<RoleAssignment> getRoleAssignmentsFromPermissions(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.permissions()
                .stream()
                .map(role ->
                        RoleAssignment
                                .builder()
                                .withRole(role)
                                .withOrganisation(DEFAULT_ADMIN_ORG)
                                .build()
                );
    }

    private Stream<RoleAssignment> getRoleAssignmentsFromDatabase(AuthenticatedUser authenticatedUser) {
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

    /**
     * Extract the OAuth2 client-id from the subject claim.
     * The subject claim structure for m2m clients is "client-id@clients"
     */
    static String clientId(String subject) {
        return subject.replace(CLIENT_SUBJECT_SUFFIX, "");
    }


    private static Stream<RoleAssignment> toRoleAssignments(M2MClient client) {
        return client.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment);
    }

    public String getClientName(AuthenticatedUser authenticatedUser) {
        String clientId = clientId(authenticatedUser.subject());
        M2MClient client = repository.getOneByPublicIdIfExists(clientId);
        if (client == null) {
            return "Entur Internal/" + clientId;
        } else {
            return client.getName();
        }
    }
}
