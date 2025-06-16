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

package no.rutebanken.baba.organisation.model.organisation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Polygon;

/**
 * Wrapper class to enable lazy loading of polygons.
 */
@Entity
public class PersistablePolygon {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persistable_polygon_seq")
  @SequenceGenerator(
    name = "persistable_polygon_seq",
    sequenceName = "persistable_polygon_seq",
    allocationSize = 1
  )
  protected Long id;

  @Basic(fetch = FetchType.LAZY)
  @Column(columnDefinition = "geometry")
  @NotNull
  private Polygon polygon;

  public Polygon getPolygon() {
    return polygon;
  }

  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
  }

  public PersistablePolygon(Polygon polygon) {
    this.polygon = polygon;
  }

  public PersistablePolygon() {}
}
