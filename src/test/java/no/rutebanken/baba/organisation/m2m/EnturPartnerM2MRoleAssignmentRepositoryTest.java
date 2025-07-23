package no.rutebanken.baba.organisation.m2m;

import org.entur.ror.permission.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static no.rutebanken.baba.organisation.m2m.EnturPartnerM2MRoleAssignmentRepository.ORG_ADMIN;
import static no.rutebanken.baba.organisation.m2m.support.M2MUtils.CLIENT_SUBJECT_SUFFIX;
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

    private static final String TEST_CLIENT_ID = "testClientId";
    private static final String TEST_ISSUER = "testIssuer";
    private static final String TEST_CLIENT_SUBJECT = TEST_CLIENT_ID + CLIENT_SUBJECT_SUFFIX;


    private EnturPartnerM2MRoleAssignmentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new EnturPartnerM2MRoleAssignmentRepository(
                new TestM2MClientRepository(null),
                Map.of(
                        ORG_ADMIN_ID, ORG_ADMIN,
                        ORG_AAA_ID, ORG_AAA,
                        ORG_BBB_ID, ORG_BBB),
                Map.of(
                        ORG_AAA, ORG_XXX + ',' + ORG_YYY,
                        ORG_BBB, ORG_XXX),
                Map.of(
                        ORG_AAA, ORG_ZZZ),
                true,
                false
        );
    }

    @Test
    void rolesForAdminOrg() {
        assertEquals(ORG_ADMIN, repository.getRutebankenOrganisationId(ORG_ADMIN_ID));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(ORG_ADMIN_ID)
                .build();

        List<RoleAssignment> roleAssignmentsForAdminOrg = repository.getRolesAssignments(authenticatedUser);
        assertEquals(1, roleAssignmentsForAdminOrg.size());
        assertEquals(RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN).withOrganisation(ORG_ADMIN).build().toString(), roleAssignmentsForAdminOrg.getFirst().toString());
    }

    @Test
    void rolesForNonAdminOrg() {
        assertEquals(ORG_AAA, repository.getRutebankenOrganisationId(ORG_AAA_ID));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(ORG_AAA_ID)
                .build();

        List<RoleAssignment> roleAssignments = repository.getRolesAssignments(authenticatedUser);
        assertEquals(6, roleAssignments.size());
        Set<String> roleAssignmentsAsString = roleAssignments.stream().map(RoleAssignment::toString).collect(Collectors.toUnmodifiableSet());
        Set<String> expectedRoleAssignmentsAsString = Set.of(
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT).withOrganisation(ORG_AAA).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_XXX).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_YYY).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_PRIVATE_DATA_VIEW).withOrganisation(ORG_XXX).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_PRIVATE_DATA_VIEW).withOrganisation(ORG_YYY).build(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_ROUTE_DATA_EDIT).withOrganisation(ORG_ZZZ).build()
        ).stream().map(RoleAssignment::toString).collect(Collectors.toUnmodifiableSet());
        assertEquals(expectedRoleAssignmentsAsString, roleAssignmentsAsString);
    }
}