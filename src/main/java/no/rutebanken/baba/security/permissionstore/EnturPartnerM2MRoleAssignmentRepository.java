package no.rutebanken.baba.security.permissionstore;

import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Build role assignments for Entur Partner machine-to-machine tokens.
 * Role assignments are built from configuration (application.properties).
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturPartnerM2MRoleAssignmentRepository {

    static final String ORG_ADMIN = "RB";

    private final Map<Long, String> rutebankenOrganisations;
    private final boolean administratorAccessActivated;
    private final Map<String, String> authorizedProvidersForNetexBlocksConsumer;
    private final Map<String, String> delegatedNetexDataProviders;


    public EnturPartnerM2MRoleAssignmentRepository(
            Map<Long, String> rutebankenOrganisations,
            Map<String, String> authorizedProvidersForNetexBlocksConsumer,
            Map<String, String> delegatedNetexDataProviders,
            boolean administratorAccessActivated) {
        this.rutebankenOrganisations = rutebankenOrganisations;
        this.authorizedProvidersForNetexBlocksConsumer = authorizedProvidersForNetexBlocksConsumer == null ? Map.of() : authorizedProvidersForNetexBlocksConsumer;
        this.delegatedNetexDataProviders = delegatedNetexDataProviders == null ? Map.of() : delegatedNetexDataProviders;
        this.administratorAccessActivated = administratorAccessActivated;

    }


    public String getRutebankenOrganisationId(long enturOrganisationId) {
        return Optional.ofNullable(rutebankenOrganisations.get(enturOrganisationId))
                .orElseThrow(() -> new IllegalArgumentException("unknown organisation " + enturOrganisationId));
    }

    public List<RoleAssignment> getRolesAssignments(long enturOrganisationId) {
        String rutebankenOrganisationId = getRutebankenOrganisationId(enturOrganisationId);
        List<RoleAssignment> roleAssignments = new ArrayList<>();

        // Add role to edit data from own organization
        String roleRouteData = administratorAccessActivated && isEnturUser(rutebankenOrganisationId)
                ? AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN
                : AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

        RoleAssignment.Builder routeDataRoleAssignmentBuilder = RoleAssignment.builder();
        routeDataRoleAssignmentBuilder.withRole(roleRouteData);
        routeDataRoleAssignmentBuilder.withOrganisation(rutebankenOrganisationId);
        roleAssignments.add(routeDataRoleAssignmentBuilder.build());

        // Add role to view NeTEx Blocks belonging to other organizations
        for (String authorizedNetexBlocksProviderForConsumer : getNetexBlocksProvidersForConsumer(rutebankenOrganisationId)) {
            RoleAssignment.Builder netexBlockRoleAssignmentBuilder = RoleAssignment.builder();
            netexBlockRoleAssignmentBuilder.withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW);
            netexBlockRoleAssignmentBuilder.withOrganisation(authorizedNetexBlocksProviderForConsumer);
            roleAssignments.add(netexBlockRoleAssignmentBuilder.build());
        }

        // Add role to edit data belonging to other organizations
        for (String delegatedNetexDataProvider : getDelegatedNetexDataProviders(rutebankenOrganisationId)) {
            RoleAssignment.Builder delegatedRouteDataRoleAssignmentBuilder = RoleAssignment.builder();
            delegatedRouteDataRoleAssignmentBuilder.withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT);
            delegatedRouteDataRoleAssignmentBuilder.withOrganisation(delegatedNetexDataProvider);
            roleAssignments.add(delegatedRouteDataRoleAssignmentBuilder.build());
        }

        return roleAssignments;


    }

    private boolean isEnturUser(String rutebankenOrganisationId) {
        return ORG_ADMIN.equals(rutebankenOrganisationId);
    }

    /**
     * Return the list of codespaces for which the organization can view NeTEx block data.
     */
    private List<String> getNetexBlocksProvidersForConsumer(String rutebankenOrganisationId) {
        String authorizedCodeSpaces = authorizedProvidersForNetexBlocksConsumer.get(rutebankenOrganisationId);
        if (!StringUtils.hasText(authorizedCodeSpaces)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(authorizedCodeSpaces.split(","));
        }

    }

    /**
     * Return the list of codespaces for which the organization can edit NeTEx data.
     */
    private List<String> getDelegatedNetexDataProviders(String rutebankenOrganisationId) {
        String authorizedCodespaces = delegatedNetexDataProviders.get(rutebankenOrganisationId);
        if (!StringUtils.hasText(authorizedCodespaces)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(authorizedCodespaces.split(","));
        }
    }


}
