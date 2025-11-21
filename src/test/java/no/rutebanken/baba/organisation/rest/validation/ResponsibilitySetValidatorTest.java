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

package no.rutebanken.baba.organisation.rest.validation;

import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityClassificationAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilityRoleAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilitySetDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResponsibilitySetValidatorTest {

  private final ResponsibilitySetValidator responsibilitySetValidator =
    new ResponsibilitySetValidator(null);

  @Test
  void validateCreateMinimalOk() {
    responsibilitySetValidator.validateCreate(minimalRespSet());
  }

  @Test
  void validateCreateWithDuplicateEventClassificationRefFails() {
    ResponsibilitySetDTO respSet = minimalRespSet();

    ResponsibilityRoleAssignmentDTO roleAssignment = respSet.roles.getFirst();
    String ref = "commonRef";
    roleAssignment.entityClassificationAssignments.add(
      new EntityClassificationAssignmentDTO(ref, true)
    );
    roleAssignment.entityClassificationAssignments.add(
      new EntityClassificationAssignmentDTO(ref, false)
    );
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> responsibilitySetValidator.validateCreate(respSet)
    );
  }

  private ResponsibilitySetDTO minimalRespSet() {
    ResponsibilitySetDTO responsibilitySet = new ResponsibilitySetDTO();
    responsibilitySet.codeSpace = "testCodeSpace";
    responsibilitySet.privateCode = "testPrivateCode";
    responsibilitySet.name = "testSet";

    ResponsibilityRoleAssignmentDTO roleAssignment = new ResponsibilityRoleAssignmentDTO();

    roleAssignment.typeOfResponsibilityRoleRef = "testRole";
    roleAssignment.responsibleOrganisationRef = "testOrg";

    responsibilitySet.roles.add(roleAssignment);

    return responsibilitySet;
  }
}
