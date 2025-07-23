package no.rutebanken.baba.organisation.m2m;

import com.google.common.collect.Streams;
import no.rutebanken.baba.organisation.m2m.support.M2MUtils;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;
import java.util.stream.Stream;

import static no.rutebanken.baba.organisation.m2m.support.M2MUtils.validateM2MClient;

/**
 * Build role assignments for Entur Internal machine-to-machine tokens.
 * Role assignments are built from the Baba database and from the "permissions" claim in the JWT token.
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturInternalM2MRoleAssignmentRepository {

    static final String DEFAULT_ADMIN_ORG = "RB";

    private final M2MClientRepository repository;
    private final boolean fromDatabaseOnly;

    public EnturInternalM2MRoleAssignmentRepository(M2MClientRepository repository, boolean fromDatabaseOnly) {
        this.repository = repository;
        this.fromDatabaseOnly = fromDatabaseOnly;
    }

    /**
     * Extract RoleAssignments from configuration and from the permission claim.
     */
    public List<RoleAssignment> getRolesAssignments(AuthenticatedUser authenticatedUser) {
        validateM2MClient(authenticatedUser);
        Stream<RoleAssignment> rolesFromDatabase = getRoleAssignmentsFromDatabase(authenticatedUser);
        Stream<RoleAssignment> rolesFromPermissions = fromDatabaseOnly ? Stream.empty() : getRoleAssignmentsFromPermissions(authenticatedUser);
        return Streams.concat(rolesFromDatabase, rolesFromPermissions).toList();
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
        return M2MUtils.getRoleAssignmentsFromDatabase(authenticatedUser, repository);
    }

    public String getClientName(AuthenticatedUser authenticatedUser) {
        String clientId = M2MUtils.clientId(authenticatedUser.subject());
        M2MClient client = repository.getOneByPublicIdIfExists(clientId);
        if (client == null) {
            return "Entur Internal/" + clientId;
        } else {
            return client.getName();
        }
    }
}
