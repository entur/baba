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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "adm_zone_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public class AdministrativeZone extends CodeSpaceEntity {

  private static final String ROLE_ASSIGNMENT_TYPE_NAME = "TopographicPlace";

  @NotNull
  private String name;

  @NotNull
  private String source;

  /**
   * Polygon is wrapped in PersistablePolygon.
   * Because we want to fetch polygons lazily and using lazy property fetching with byte code enhancement breaks tests.
   */
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private PersistablePolygon polygon;

  @NotNull
  @Enumerated(EnumType.STRING)
  private AdministrativeZoneType administrativeZoneType;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Polygon getPolygon() {
    if (polygon == null) {
      return null;
    }
    return polygon.getPolygon();
  }

  public void setPolygon(Polygon polygon) {
    if (polygon == null) {
      this.polygon = null;
    } else {
      this.polygon = new PersistablePolygon(polygon);
    }
  }

  public AdministrativeZoneType getAdministrativeZoneType() {
    return administrativeZoneType;
  }

  public void setAdministrativeZoneType(AdministrativeZoneType administrativeZoneType) {
    this.administrativeZoneType = administrativeZoneType;
  }

  /**
   * Return ID for this admin zone when referred to in a role assignment.
   *
   */
  @Transient
  public String getRoleAssignmentId() {
    return String.join(":", getSource(), ROLE_ASSIGNMENT_TYPE_NAME, getPrivateCode());
  }
}
