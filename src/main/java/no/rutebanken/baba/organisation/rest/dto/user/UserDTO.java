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

package no.rutebanken.baba.organisation.rest.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.rest.dto.BaseDTO;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilitySetDTO;

@Schema(description = "User")
public class UserDTO extends BaseDTO {

  public String username;

  public boolean personalAccount;

  @Schema(description = "Id of the organisation the user belongs to")
  public String organisationRef;

  @Schema(
    description = "References to the set of responsibility sets describing the users authorizations"
  )
  public List<String> responsibilitySetRefs = new ArrayList<>();

  public ContactDetailsDTO contactDetails;

  @Schema(
    description = "Fully mapped object for ease of use. Disregarded in CRUD operations (use reference)"
  )
  public Set<NotificationConfigDTO> notifications = new HashSet<>();

  @Schema(
    description = "Fully mapped object for ease of use. Disregarded in CRUD operations (use reference)"
  )
  public OrganisationDTO organisation;

  @Schema(
    description = "Fully mapped object for ease of use. Disregarded in CRUD operations (use reference)"
  )
  public List<ResponsibilitySetDTO> responsibilitySets;
}
