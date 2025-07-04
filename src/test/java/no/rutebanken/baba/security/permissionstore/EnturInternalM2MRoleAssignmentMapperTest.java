package no.rutebanken.baba.security.permissionstore;

import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.RoleAssignment;

import java.util.List;

import static no.rutebanken.baba.security.permissionstore.EnturInternalM2MRoleAssignmentMapper.DEFAULT_ADMIN_ORG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnturInternalM2MRoleAssignmentMapperTest {

    @Test
    void mapEmptyRoleAssignments() {
        EnturInternalM2MRoleAssignmentMapper mapper = new EnturInternalM2MRoleAssignmentMapper();
        assertTrue(mapper.getRolesAssignments(List.of()).isEmpty());
    }

    @Test
    void mapRoleAssignments() {
        EnturInternalM2MRoleAssignmentMapper mapper = new EnturInternalM2MRoleAssignmentMapper();
        List<String> permissions = List.of("a", "b", "c");
        List<String> roleAssignments = List.of(
                roleAssignment("a").toString(),
                roleAssignment("b").toString(),
                roleAssignment("c").toString()
        );
        assertEquals(roleAssignments, mapper.getRolesAssignments(permissions).stream().map(RoleAssignment::toString).toList());
    }

    private static RoleAssignment roleAssignment(String a) {
        return RoleAssignment.builder().withOrganisation(DEFAULT_ADMIN_ORG).withRole(a).build();
    }

}