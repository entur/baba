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
import org.springframework.util.CollectionUtils;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "org_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public abstract class Organisation extends CodeSpaceEntity {

  private Long companyNumber;

  @NotNull
  private String name;

  @OneToMany(cascade = CascadeType.ALL)
  private Set<OrganisationPart> parts;

  public Long getCompanyNumber() {
    return companyNumber;
  }

  public void setCompanyNumber(Long companyNumber) {
    this.companyNumber = companyNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<OrganisationPart> getParts() {
    if (parts == null) {
      this.parts = new HashSet<>();
    }
    return parts;
  }

  public void setParts(Set<OrganisationPart> parts) {
    getParts().clear();
    getParts().addAll(parts);
  }

  public OrganisationPart getOrganisationPart(String id) {
    if (id != null && !CollectionUtils.isEmpty(parts)) {
      for (OrganisationPart existingPart : parts) {
        if (id.equals(existingPart.getId())) {
          return existingPart;
        }
      }
    }
    throw new IllegalArgumentException(
      getClass().getSimpleName() + " with id: " + id + " not found"
    );
  }
}
