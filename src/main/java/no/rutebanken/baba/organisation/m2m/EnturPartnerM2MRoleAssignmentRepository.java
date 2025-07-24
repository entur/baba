package no.rutebanken.baba.organisation.m2m;

import com.google.common.collect.Streams;
import no.rutebanken.baba.organisation.m2m.support.M2MUtils;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.rutebanken.baba.organisation.m2m.support.M2MUtils.validateM2MClient;

/**
 * Build role assignments for Entur Partner machine-to-machine tokens.
 * Role assignments are built from the baba database and from configuration (application.properties).
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturPartnerM2MRoleAssignmentRepository {

    static final String ORG_ADMIN = "RB";

    private static final Logger LOGGER = LoggerFactory.getLogger(EnturPartnerM2MRoleAssignmentRepository.class);


    private final Map<Long, String> rutebankenOrganisations;
    private final boolean administratorAccessActivated;
    private final Map<String, String> authorizedProvidersForNetexBlocksConsumer;
    private final Map<String, String> delegatedNetexDataProviders;

    private final M2MClientRepository repository;
    private final boolean fromDatabaseOnly;


    public EnturPartnerM2MRoleAssignmentRepository(
            M2MClientRepository repository, Map<Long, String> rutebankenOrganisations,
            Map<String, String> authorizedProvidersForNetexBlocksConsumer,
            Map<String, String> delegatedNetexDataProviders,
            boolean administratorAccessActivated,
            boolean fromDatabaseOnly) {
        this.rutebankenOrganisations = rutebankenOrganisations;
        this.authorizedProvidersForNetexBlocksConsumer = authorizedProvidersForNetexBlocksConsumer == null ? Map.of() : authorizedProvidersForNetexBlocksConsumer;
        this.delegatedNetexDataProviders = delegatedNetexDataProviders == null ? Map.of() : delegatedNetexDataProviders;
        this.administratorAccessActivated = administratorAccessActivated;

        this.repository = repository;
        this.fromDatabaseOnly = fromDatabaseOnly;
    }


    public String getRutebankenOrganisationId(long enturOrganisationId) {
        return Optional.ofNullable(rutebankenOrganisations.get(enturOrganisationId))
                .orElseThrow(() -> new IllegalArgumentException("unknown organisation " + enturOrganisationId));
    }

    public List<RoleAssignment> getRolesAssignments(AuthenticatedUser authenticatedUser) {
        validateM2MClient(authenticatedUser);
        List<RoleAssignment> rolesFromDatabase = getRoleAssignmentsFromDatabase(authenticatedUser);
        List<RoleAssignment> rolesFromConfiguration = fromDatabaseOnly ? List.of() : getRoleAssignmentsFromConfiguration(authenticatedUser);
        LOGGER.info("Returning {} role assignments from database and {} role assignments from configuration for client {} and organisation {}",
                rolesFromDatabase.size(), rolesFromConfiguration.size(), authenticatedUser.subject(), authenticatedUser.organisationId());

        return Streams.concat(rolesFromDatabase.stream(), rolesFromConfiguration.stream()).toList();

    }

    /**
     * TODO Permission Store migration: obsolete, to be removed after migration, role assignments should be extracted only from the database.
     */
    private List<RoleAssignment> getRoleAssignmentsFromConfiguration(AuthenticatedUser authenticatedUser) {
        long enturOrganisationId = authenticatedUser.organisationId();
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

        // Add role to view private NeTEx data belonging to other organizations
        for (
                String authorizedNetexBlocksProviderForConsumer :

                getNetexBlocksProvidersForConsumer(rutebankenOrganisationId)) {
            RoleAssignment viewNetexBlock = RoleAssignment.builder()
                    .withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW)
                    .withOrganisation(authorizedNetexBlocksProviderForConsumer)
                    .build();
            roleAssignments.add(viewNetexBlock);
            RoleAssignment viewPrivateNetexData = RoleAssignment.builder()
                    .withRole(AuthorizationConstants.ROLE_NETEX_PRIVATE_DATA_VIEW)
                    .withOrganisation(authorizedNetexBlocksProviderForConsumer)
                    .build();
            roleAssignments.add(viewPrivateNetexData);
        }

        // Add role to edit data belonging to other organizations
        for (
                String delegatedNetexDataProvider :

                getDelegatedNetexDataProviders(rutebankenOrganisationId)) {
            RoleAssignment.Builder delegatedRouteDataRoleAssignmentBuilder = RoleAssignment.builder();
            delegatedRouteDataRoleAssignmentBuilder.withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT);
            delegatedRouteDataRoleAssignmentBuilder.withOrganisation(delegatedNetexDataProvider);
            roleAssignments.add(delegatedRouteDataRoleAssignmentBuilder.build());
        }

        return roleAssignments;

    }

    private List<RoleAssignment> getRoleAssignmentsFromDatabase(AuthenticatedUser authenticatedUser) {
        return M2MUtils.getRoleAssignmentsFromDatabase(authenticatedUser, repository);
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
