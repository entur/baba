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

import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.rest.dto.organisation.AdministrativeZoneDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AdministrativeZoneValidator
  implements DTOValidator<AdministrativeZone, AdministrativeZoneDTO> {

  @Override
  public void validateCreate(AdministrativeZoneDTO dto) {
    Assert.hasLength(dto.privateCode, "privateCode required");
    Assert.hasLength(dto.codeSpace, "codeSpace required");
    assertCommon(dto);
  }

  @Override
  public void validateUpdate(AdministrativeZoneDTO dto, AdministrativeZone entity) {
    assertCommon(dto);
  }

  private void assertCommon(AdministrativeZoneDTO dto) {
    Assert.hasLength(dto.name, "name required");
    Assert.notNull(dto.polygon, "polygon required");
    Assert.notNull(dto.source, "source required");
    Assert.notNull(dto.type, "type required");
  }
}
