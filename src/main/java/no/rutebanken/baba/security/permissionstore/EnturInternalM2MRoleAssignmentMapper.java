package no.rutebanken.baba.security.permissionstore;

import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;

/**
 * Build role assignments for Entur Internal machine-to-machine tokens.
 */
public class EnturInternalM2MRoleAssignmentMapper {

    static final String DEFAULT_ADMIN_ORG = "RB";

    /**
     * Extract RoleAssignments from the permission claim.
     * Internal tokens (from Entur Internal) contain cross-organization roles under this claim.
     */
    public List<RoleAssignment> getRolesAssignments(List<String> permissions) {
        return permissions
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

}
