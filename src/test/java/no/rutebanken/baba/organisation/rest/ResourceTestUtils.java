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

package no.rutebanken.baba.organisation.rest;

import static no.rutebanken.baba.organisation.TestConstantsOrganisation.CODE_SPACE_ID;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZoneType;
import no.rutebanken.baba.organisation.rest.dto.TypeDTO;
import no.rutebanken.baba.organisation.rest.dto.organisation.AdministrativeZoneDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Polygon;

public class ResourceTestUtils {

  public static void setNotificationConfig(
    TestRestTemplate restTemplate,
    String userName,
    Set<NotificationConfigDTO> config
  ) {
    restTemplate.put(
      "/services/organisations/users/" + userName + "/notification_configurations",
      config,
      String.class
    );
  }

  public static List<String> addAdminZones(TestRestTemplate restTemplate, String... privateCodes) {
    List<String> ids = new ArrayList<>();
    for (String privateCode : privateCodes) {
      ids.add(addAdminZone(restTemplate, privateCode));
    }

    return ids;
  }

  public static String addAdminZone(TestRestTemplate restTemplate, String privateCode) {
    AdministrativeZoneDTO updateAdministrativeZone = createAdministrativeZone(
      privateCode,
      privateCode,
      validPolygon()
    );
    URI uri = restTemplate.postForLocation(
      "/services/organisations/administrative_zones",
      updateAdministrativeZone
    );
    return new File(uri.getPath()).getName();
  }

  public static void assertType(TypeDTO in, URI uri, TestRestTemplate restTemplate) {
    Assertions.assertNotNull(uri);
    ResponseEntity<TypeDTO> rsp = restTemplate.getForEntity(uri, TypeDTO.class);
    TypeDTO out = rsp.getBody();
    Assertions.assertEquals(in.name, out.name);
    Assertions.assertEquals(in.privateCode, out.privateCode);
  }

  public static AdministrativeZoneDTO createAdministrativeZone(
    String name,
    String privateCode,
    Polygon polygon
  ) {
    AdministrativeZoneDTO administrativeZone = new AdministrativeZoneDTO();
    administrativeZone.name = name;
    administrativeZone.privateCode = privateCode;
    administrativeZone.polygon = polygon;
    administrativeZone.codeSpace = CODE_SPACE_ID;
    administrativeZone.type = AdministrativeZoneType.CUSTOM;
    administrativeZone.source = "KVE";
    return administrativeZone;
  }

  public static Polygon validPolygon() {
    double[][][] coordinates = new double[][][] {
      { { 1.0, 1.0 }, { 1.0, 2.0 }, { 2.0, 2.0 }, { 1.0, 1.0 } },
      { { 1.0, 1.0 }, { 1.0, 2.0 }, { 2.0, 2.0 }, { 1.0, 1.0 } },
    };
    return new Polygon(coordinates);
  }
}
