package no.rutebanken.baba.organisation.m2m;

import com.google.common.collect.Streams;
import no.rutebanken.baba.organisation.m2m.support.M2MUtils;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static no.rutebanken.baba.organisation.m2m.support.M2MUtils.validateM2MClient;

/**
 * Build role assignments for Entur Internal machine-to-machine tokens.
 * Role assignments are built from the Baba database and from the "permissions" claim in the JWT token.
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturInternalM2MRoleAssignmentRepository {

    static final String DEFAULT_ADMIN_ORG = "RB";

    private static final Logger LOGGER = LoggerFactory.getLogger(EnturInternalM2MRoleAssignmentRepository.class);


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
        List<RoleAssignment> rolesFromDatabase = getRoleAssignmentsFromDatabase(authenticatedUser);
        List<RoleAssignment> rolesFromPermissions = fromDatabaseOnly ? List.of() : getRoleAssignmentsFromPermissions(authenticatedUser);
        LOGGER.info("Returning {} role assignments from database and {} role assignments from permissions for client {} and organisation {}",
                rolesFromDatabase.size(), rolesFromPermissions.size(), authenticatedUser.subject(), authenticatedUser.organisationId());
        return Streams.concat(rolesFromDatabase.stream(), rolesFromPermissions.stream()).toList();
    }

    /**
     * Internal clients (from Entur Internal) owned by RoR contain cross-organization roles under the permission claim.
     * TODO Permission Store migration: obsolete, to be removed after migration, role assignments should be extracted only from the database.
     */
    private static List<RoleAssignment> getRoleAssignmentsFromPermissions(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.permissions()
                .stream()
                .map(role ->
                        RoleAssignment
                                .builder()
                                .withRole(role)
                                .withOrganisation(DEFAULT_ADMIN_ORG)
                                .build()
                )
                .toList();
    }

    private List<RoleAssignment> getRoleAssignmentsFromDatabase(AuthenticatedUser authenticatedUser) {
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
