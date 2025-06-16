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

package no.rutebanken.baba.organisation.model.user.eventfilter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassification;

/**
 * User defined filter for Crud/changelog events.
 */
@Entity
public class CrudEventFilter extends EventFilter {

  @ManyToMany(fetch = FetchType.EAGER)
  @NotNull
  private Set<EntityClassification> entityClassifications;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<AdministrativeZone> administrativeZones;

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

  public Set<EntityClassification> getEntityClassifications() {
    if (entityClassifications == null) {
      entityClassifications = new HashSet<>();
    }
    return entityClassifications;
  }

  public void setEntityClassifications(Set<EntityClassification> entityClassifications) {
    getEntityClassifications().clear();
    getEntityClassifications().addAll(entityClassifications);
  }

  @PreRemove
  private void removeConnections() {
    getEntityClassifications().clear();
    getAdministrativeZones().clear();
  }
}
