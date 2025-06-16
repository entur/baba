/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */

package no.rutebanken.baba.organisation.repository;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ResponsibilitySetRepositoryTest extends BaseIntegrationTest {

  @Autowired
  private ResponsibilitySetRepository responsibilitySetRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void findResponsibilitySetsReferringToRole() {
    Role role1 = createRole("r1");
    Role role2 = createRole("r2");

    ResponsibilitySet only1 = createSet("setOnly1", createRoleAssignment(role1));
    ResponsibilitySet only2 = createSet("setOnly2", createRoleAssignment(role2));
    ResponsibilitySet both = createSet(
      "setBoth",
      createRoleAssignment(role1),
      createRoleAssignment(role2)
    );

    Assertions.assertEquals(
      Set.of(only1, both),
      Set.copyOf(responsibilitySetRepository.getResponsibilitySetsReferringTo(role1))
    );
    Assertions.assertEquals(
      Set.of(only2, both),
      Set.copyOf(responsibilitySetRepository.getResponsibilitySetsReferringTo(role2))
    );
  }

  private ResponsibilitySet createSet(String name, ResponsibilityRoleAssignment... roles) {
    ResponsibilitySet responsibilitySet = new ResponsibilitySet();

    responsibilitySet.setName(name);
    responsibilitySet.setPrivateCode(name);
    responsibilitySet.setCodeSpace(defaultCodeSpace);

    if (roles != null) {
      responsibilitySet.getRoles().addAll(Arrays.asList(roles));
    }

    return responsibilitySetRepository.save(responsibilitySet);
  }

  private ResponsibilityRoleAssignment createRoleAssignment(Role role) {
    return ResponsibilityRoleAssignment
      .builder()
      .withCodeSpace(defaultCodeSpace)
      .withPrivateCode(UUID.randomUUID().toString())
      .withResponsibleOrganisation(defaultOrganisation)
      .withTypeOfResponsibilityRole(role)
      .build();
  }

  private Role createRole(String roleName) {
    Role role = new Role();
    role.setPrivateCode(roleName);
    role.setName(roleName);
    return roleRepository.save(role);
  }
}
