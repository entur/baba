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
import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "org_part_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public class OrganisationPart extends CodeSpaceEntity {

  @NotNull
  private String name;

  @ManyToMany
  private Set<AdministrativeZone> administrativeZones;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<AdministrativeZone> getAdministrativeZones() {
    if (administrativeZones == null) {
      administrativeZones = new HashSet<>();
    }
    return administrativeZones;
  }

  public void setAdministrativeZones(Set<AdministrativeZone> administrativeZones) {
    getAdministrativeZones().clear();
    getAdministrativeZones().addAll(administrativeZones);
  }

  @PreRemove
  private void removeResponsibilitySetConnections() {
    if (administrativeZones != null) {
      administrativeZones.clear();
    }
  }
}
