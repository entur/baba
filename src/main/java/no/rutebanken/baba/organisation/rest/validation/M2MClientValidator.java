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

import java.util.Set;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class M2MClientValidator implements DTOValidator<M2MClient, M2MClientDTO> {

  @Override
  public void validateCreate(M2MClientDTO dto) {
    Assert.hasLength(dto.privateCode, "private code required");
    validateUpdate(dto, null);
  }

  @Override
  public void validateUpdate(M2MClientDTO dto, M2MClient entity) {
    Assert.hasLength(dto.name, "display name required");
    Assert.hasLength(dto.issuer, "issuer required");
    Assert.isTrue(
      Set.of(M2MClient.PARTNER_ISSUER, M2MClient.INTERNAL_ISSUER).contains(dto.issuer),
      "Issuer is either 'Partner' or 'Internal'"
    );
    Assert.notNull(dto.enturOrganisationId, "enturOrganisationId required");
  }
}
