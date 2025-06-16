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

package no.rutebanken.baba.organisation.model.responsibility;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import no.rutebanken.baba.organisation.model.TypeEntity;
import no.rutebanken.baba.organisation.model.VersionedEntity;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(name = "role_unique_id", columnNames = { "privateCode", "entityVersion" }),
  }
)
public class Role extends VersionedEntity implements TypeEntity {

  @NotNull
  private String name;

  public Role() {}

  public Role(String privateCode, String name) {
    setPrivateCode(privateCode);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
