package no.rutebanken.baba.security.permissionstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static no.rutebanken.baba.security.permissionstore.EnturPartnerM2MRoleAssignmentRepository.ORG_ADMIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnturPartnerM2MRoleAssignmentRepositoryTest {

    private static final String ORG_AAA = "AAA";
    private static final String ORG_BBB = "BBB";
    private static final long ORG_ADMIN_ID = 1L;
    private static final long ORG_AAA_ID = 2L;
    private static final long ORG_BBB_ID = 3L;
    private static final String ORG_XXX = "XXX";
    private static final String ORG_ZZZ = "ZZZ";
    private static final String ORG_YYY = "YYY";

    private EnturPartnerM2MRoleAssignmentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EnturPartnerM2MRoleAssignmentRepository(
                Map.of(
                        ORG_ADMIN_ID, ORG_ADMIN,
                        ORG_AAA_ID, ORG_AAA,
                        ORG_BBB_ID, ORG_BBB),
                Map.of(
                        ORG_AAA, ORG_XXX + ',' + ORG_YYY,
                        ORG_BBB, ORG_XXX),
                Map.of(
                        ORG_AAA, ORG_ZZZ),
                true
        );
    }

    @Test
    void rolesForAdminOrg() {
        assertEquals(ORG_ADMIN, repository.getRutebankenOrganisationId(ORG_ADMIN_ID));
        List<RoleAssignment> roleAssignmentsForAdminOrg = repository.getRolesAssignments(ORG_ADMIN_ID);
        assertEquals(1, roleAssignmentsForAdminOrg.size());
        assertEquals(RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN).withOrganisation(ORG_ADMIN).build().toString(), roleAssignmentsForAdminOrg.getFirst().toString());
    }

    @Test
    void rolesForNonAdminOrg() {
        assertEquals(ORG_AAA, repository.getRutebankenOrganisationId(ORG_AAA_ID));
        List<RoleAssignment> roleAssignments = repository.getRolesAssignments(ORG_AAA_ID);
        assertEquals(4, roleAssignments.size());
        Set<String> roleAssignmentsAsString = roleAssignments.stream().map(RoleAssignment::toString).collect(Collectors.toUnmodifiableSet());
        Set<String> expectedRoleAssignmentsAsString = Set.of(
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT).withOrganisation(ORG_AAA).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_XXX).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_YYY).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT).withOrganisation(ORG_ZZZ).build()
        ).stream().map(RoleAssignment::toString).collect(Collectors.toUnmodifiableSet());
        assertEquals(expectedRoleAssignmentsAsString, roleAssignmentsAsString);
    }
}