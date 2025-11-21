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

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.ContactDetails;
import no.rutebanken.baba.organisation.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ResponsibilitySetRepository responsibilitySetRepository;

  @Autowired
  private EntityManager em;

  @Test
  void testInsertUser() {
    User user = User
      .builder()
      .withUsername("raffen")
      .withPrivateCode("2")
      .withOrganisation(defaultOrganisation)
      .withContactDetails(minimalContactDetails())
      .build();
    User createdUser = userRepository.saveAndFlush(user);

    User fetchedUser = userRepository.getReferenceById(createdUser.getPk());
    Assertions.assertEquals("User:2", fetchedUser.getId());
  }

  protected ContactDetails minimalContactDetails() {
    ContactDetails contactDetails = new ContactDetails();
    contactDetails.setEmail("valid@email.org");
    return contactDetails;
  }

  @Test
  void testFindByResponsibilitySet() {
    Role role = roleRepository.save(new Role("testCode", "testRole"));
    ResponsibilityRoleAssignment responsibilityRoleAssignment = ResponsibilityRoleAssignment
      .builder()
      .withPrivateCode("pCode")
      .withResponsibleOrganisation(defaultOrganisation)
      .withTypeOfResponsibilityRole(role)
      .withCodeSpace(defaultCodeSpace)
      .build();
    ResponsibilitySet responsibilitySet = new ResponsibilitySet(
      defaultCodeSpace,
      "pCode",
      "name",
      Set.of(responsibilityRoleAssignment)
    );

    responsibilitySet = responsibilitySetRepository.save(responsibilitySet);

    User userWithRespSet = userRepository.saveAndFlush(
      User
        .builder()
        .withUsername("userWithRespSet")
        .withPrivateCode("userWithRespSet")
        .withOrganisation(defaultOrganisation)
        .withResponsibilitySets(Set.of(responsibilitySet))
        .withContactDetails(minimalContactDetails())
        .build()
    );
    User userWithoutRespSet = userRepository.saveAndFlush(
      User
        .builder()
        .withUsername("userWithoutRespSet")
        .withPrivateCode("userWithoutRespSet")
        .withOrganisation(defaultOrganisation)
        .withContactDetails(minimalContactDetails())
        .build()
    );
    List<User> usersWithRespSet = userRepository.findUsersWithResponsibilitySet(responsibilitySet);

    Assertions.assertEquals(1, usersWithRespSet.size());
    Assertions.assertTrue(usersWithRespSet.contains(userWithRespSet));
    Assertions.assertFalse(usersWithRespSet.contains(userWithoutRespSet));

    userRepository.delete(usersWithRespSet.getFirst());

    em.flush();
  }
}
