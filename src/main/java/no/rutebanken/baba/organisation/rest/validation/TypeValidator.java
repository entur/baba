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

import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.repository.ResponsibilitySetRepository;
import no.rutebanken.baba.organisation.rest.dto.TypeDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class TypeValidator<E extends VersionedEntity> implements DTOValidator<E, TypeDTO> {

  private final ResponsibilitySetRepository responsibilitySetRepository;

  public TypeValidator(ResponsibilitySetRepository responsibilitySetRepository) {
    this.responsibilitySetRepository = responsibilitySetRepository;
  }

  @Override
  public void validateCreate(TypeDTO dto) {
    Assert.hasLength(dto.name, "name required");
    Assert.hasLength(dto.privateCode, "privateCode required");
  }

  @Override
  public void validateUpdate(TypeDTO dto, E entity) {
    Assert.hasLength(dto.name, "name required");
  }

  @Override
  public void validateDelete(E entity) {
    Assert.isTrue(
      responsibilitySetRepository.getResponsibilitySetsReferringTo(entity).isEmpty(),
      "referred to by responsibilitySet"
    );
  }
}
