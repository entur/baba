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

package no.rutebanken.baba.organisation.rest.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZoneType;
import no.rutebanken.baba.organisation.rest.dto.BaseDTO;
import org.wololo.geojson.Polygon;

@Schema(description = "A named geographical area.")
public class AdministrativeZoneDTO extends BaseDTO {

  public String name;

  @Schema(
    description = "Source of the data. Should be a code space styled value, ala KVE (Kartverket) or WOF (whosonfirst)"
  )
  public String source;

  @Schema(description = "Geojson polygon encoded value describing the surface of the admin zone")
  public Polygon polygon;

  public AdministrativeZoneType type;
}
