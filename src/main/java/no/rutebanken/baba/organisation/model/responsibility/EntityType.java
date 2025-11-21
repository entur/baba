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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.TypeEntity;
import org.springframework.util.CollectionUtils;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "entity_type_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public class EntityType extends CodeSpaceEntity implements TypeEntity {

  @NotNull
  private String name;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getType() {
    return "TypeOfEntity";
  }

  @OneToMany(
    mappedBy = "entityType",
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private Set<EntityClassification> classifications;

  public Set<EntityClassification> getClassifications() {
    if (classifications == null) {
      this.classifications = new HashSet<>();
    }
    return classifications;
  }

  public void setClassifications(Set<EntityClassification> classifications) {
    getClassifications().clear();
    getClassifications().addAll(classifications);
  }

  public EntityClassification getClassification(String privateCode) {
    if (privateCode != null && !CollectionUtils.isEmpty(classifications)) {
      for (EntityClassification existingClassification : classifications) {
        if (privateCode.equals(existingClassification.getPrivateCode())) {
          return existingClassification;
        }
      }
    }
    return null;
  }
}
