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

import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityClassificationAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilityRoleAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilitySetDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Service
public class ResponsibilitySetValidator
  implements DTOValidator<ResponsibilitySet, ResponsibilitySetDTO> {

  private final UserRepository userRepository;

  public ResponsibilitySetValidator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void validateCreate(ResponsibilitySetDTO dto) {
    Assert.hasLength(dto.codeSpace, "codeSpace required");
    Assert.hasLength(dto.privateCode, "privateCode required");
    assertCommon(dto);
  }

  @Override
  public void validateUpdate(ResponsibilitySetDTO dto, ResponsibilitySet entity) {
    assertCommon(dto);
  }

  private void assertCommon(ResponsibilitySetDTO dto) {
    Assert.hasLength(dto.name, "name required");
    Assert.notEmpty(dto.roles, "roles required");
    for (ResponsibilityRoleAssignmentDTO roleDto : dto.roles) {
      Assert.notNull(roleDto, "roles cannot be empty");
      Assert.hasLength(
        roleDto.typeOfResponsibilityRoleRef,
        "roles.typeOfResponsibilityRoleRef required"
      );
      Assert.hasLength(
        roleDto.responsibleOrganisationRef,
        "roles.responsibleOrganisationRef required"
      );

      if (!CollectionUtils.isEmpty(roleDto.entityClassificationAssignments)) {
        Set<String> uniqueClassificationIds = new HashSet<>();
        for (EntityClassificationAssignmentDTO assignmentDTO : roleDto.entityClassificationAssignments) {
          Assert.hasLength(
            assignmentDTO.entityClassificationRef,
            "roles.entityClassificationAssignments.entityClassificationRef required"
          );

          Assert.isTrue(
            uniqueClassificationIds.add(assignmentDTO.entityClassificationRef),
            "roles.entityClassificationAssignments.entityClassificationRe must be unique"
          );
        }
      }
    }
  }

  @Override
  public void validateDelete(ResponsibilitySet entity) {
    Assert.isTrue(
      userRepository.findUsersWithResponsibilitySet(entity).isEmpty(),
      "referred to by user"
    );
  }
}
