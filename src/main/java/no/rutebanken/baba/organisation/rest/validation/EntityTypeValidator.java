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

import no.rutebanken.baba.organisation.model.responsibility.EntityType;
import no.rutebanken.baba.organisation.rest.dto.TypeDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityTypeDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class EntityTypeValidator implements DTOValidator<EntityType, EntityTypeDTO> {

  @Override
  public void validateCreate(EntityTypeDTO dto) {
    Assert.hasLength(dto.privateCode, "privateCode required");
    assertCommon(dto);
  }

  @Override
  public void validateUpdate(EntityTypeDTO dto, EntityType entity) {
    assertCommon(dto);
  }

  private void assertCommon(EntityTypeDTO dto) {
    Assert.hasLength(dto.name, "name required");

    if (dto.classifications != null) {
      for (TypeDTO classification : dto.classifications) {
        Assert.hasLength(classification.name, "classifications.name required");
        if (classification.id == null) {
          Assert.hasLength(
            classification.privateCode,
            "classifications.privateCode or classifications.id required"
          );
        }
      }
    }
  }
}
