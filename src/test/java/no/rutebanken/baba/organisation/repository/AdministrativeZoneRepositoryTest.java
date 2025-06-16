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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZoneType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;

class AdministrativeZoneRepositoryTest extends BaseIntegrationTest {

  @Autowired
  private AdministrativeZoneRepository administrativeZoneRepository;

  @Test
  void testInsertAdministrativeZone() {
    AdministrativeZone zone = new AdministrativeZone();
    zone.setPrivateCode("0101");
    zone.setName("name");
    zone.setCodeSpace(defaultCodeSpace);
    zone.setAdministrativeZoneType(AdministrativeZoneType.COUNTY);
    zone.setSource("KVE");

    GeometryFactory fact = new GeometryFactory();
    LinearRing linear = new GeometryFactory()
      .createLinearRing(
        new Coordinate[] {
          new Coordinate(0, 0),
          new Coordinate(1, 0),
          new Coordinate(1, 1),
          new Coordinate(0, 0),
        }
      );
    Polygon poly = new Polygon(linear, null, fact);

    zone.setPolygon(poly);

    administrativeZoneRepository.saveAndFlush(zone);
    assertNotNull(administrativeZoneRepository.getOneByPublicIdIfExists(zone.getId()));
  }
}
