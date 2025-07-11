package no.rutebanken.baba.security.permissionstore;

import org.entur.ror.permission.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;
import java.util.Map;

import static no.rutebanken.baba.security.permissionstore.EnturInternalM2MRoleAssignmentRepository.CLIENT_SUBJECT_SUFFIX;
import static no.rutebanken.baba.security.permissionstore.EnturInternalM2MRoleAssignmentRepository.DEFAULT_ADMIN_ORG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnturInternalM2MRoleAssignmentRepositoryTest {

    private static final String TEST_CLIENT_ID = "testClientId";
    private static final String TEST_ISSUER = "testIssuer";
    private static final String TEST_CLIENT_SUBJECT = TEST_CLIENT_ID + CLIENT_SUBJECT_SUFFIX;
    private static final String ORG_AAA = "AAA";
    private static final String ORG_BBB = "BBB";

    @Test
    void mapEmptyRoleAssignments() {
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(Map.of());
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .build();
        assertTrue(repository.getRolesAssignments(authenticatedUser).isEmpty());
    }

    @Test
    void mapRoleAssignmentsFromPermissions() {
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(Map.of());
        List<String> roleAssignments = List.of(
                roleAssignment("a").toString(),
                roleAssignment("b").toString(),
                roleAssignment("c").toString()
        );
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withPermissions(List.of("a", "b", "c"))
                .build();
        assertEquals(roleAssignments, repository.getRolesAssignments(authenticatedUser).stream().map(RoleAssignment::toString).toList());
    }

    @Test
    void mapRoleAssignmentsFromConfiguration() {
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(Map.of(
                TEST_CLIENT_ID, ORG_AAA + ',' + ORG_BBB
        ));
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .build();

        List<String> expectedRoleAssignments = List.of(
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_AAA).build().toString(),
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_BBB).build().toString()
        );

        assertEquals(expectedRoleAssignments, repository.getRolesAssignments(authenticatedUser).stream().map(RoleAssignment::toString).toList());
    }


    private static RoleAssignment roleAssignment(String a) {
        return RoleAssignment.builder().withOrganisation(DEFAULT_ADMIN_ORG).withRole(a).build();
    }

}