package no.rutebanken.baba.organisation.util;

import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassification;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassificationAssignment;
import no.rutebanken.baba.organisation.model.responsibility.EntityType;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.RoleAssignment;

class RoleAssignmentMapperTest {

  @Test
  void testMapResponsibilityRoleAssignmentToIamRoleAssignment() {
    ResponsibilityRoleAssignment orgRegRoleAssignment = new ResponsibilityRoleAssignment();
    Role role = new Role();
    role.setPrivateCode("testRole");
    Organisation organisation = new Authority();
    organisation.setPrivateCode("testOrg");

    EntityType entityType = new EntityType();
    entityType.setPrivateCode("StopPlace");

    EntityClassification entityClassification = new EntityClassification();
    entityClassification.setPrivateCode(EntityClassification.ALL_TYPES);
    entityClassification.setEntityType(entityType);
    EntityClassificationAssignment entityClassificationAssignment =
      new EntityClassificationAssignment(entityClassification, orgRegRoleAssignment, true);
    orgRegRoleAssignment.getResponsibleEntityClassifications().add(entityClassificationAssignment);

    EntityClassification entityClassificationNegated = new EntityClassification();
    entityClassificationNegated.setPrivateCode("buss");
    entityClassificationNegated.setEntityType(entityType);
    EntityClassificationAssignment entityClassificationAssignmentNegated =
      new EntityClassificationAssignment(entityClassificationNegated, orgRegRoleAssignment, false);
    orgRegRoleAssignment
      .getResponsibleEntityClassifications()
      .add(entityClassificationAssignmentNegated);

    orgRegRoleAssignment.setTypeOfResponsibilityRole(role);
    orgRegRoleAssignment.setResponsibleOrganisation(organisation);

    AdministrativeZone administrativeZone = new AdministrativeZone();
    administrativeZone.setSource("KVE");
    administrativeZone.setPrivateCode("05");
    orgRegRoleAssignment.setResponsibleArea(administrativeZone);

    RoleAssignment iamRoleAssignment = RoleAssignmentMapper.toRoleAssignment(orgRegRoleAssignment);

    Assertions.assertEquals(role.getPrivateCode(), iamRoleAssignment.getRole());
    Assertions.assertEquals(organisation.getPrivateCode(), iamRoleAssignment.getOrganisation());

    Set<String> expectedCodes = Set.of(
      entityClassification.getPrivateCode(),
      "!" + entityClassificationNegated.getPrivateCode()
    );
    List<String> classificationCodeList = iamRoleAssignment
      .getEntityClassifications()
      .get(entityType.getPrivateCode());
    Assertions.assertEquals(expectedCodes, Set.copyOf(classificationCodeList));

    Assertions.assertEquals("KVE:TopographicPlace:05", iamRoleAssignment.getAdministrativeZone());
  }
}
