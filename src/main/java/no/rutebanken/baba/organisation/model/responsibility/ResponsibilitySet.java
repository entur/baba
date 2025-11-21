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
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import org.springframework.util.CollectionUtils;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "responsibility_set_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public class ResponsibilitySet extends CodeSpaceEntity {

  @NotNull
  private String name;

  @NotNull
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Set<ResponsibilityRoleAssignment> roles;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<ResponsibilityRoleAssignment> getRoles() {
    if (roles == null) {
      this.roles = new HashSet<>();
    }
    return roles;
  }

  public void setRoles(Set<ResponsibilityRoleAssignment> roles) {
    if (this.roles == null) {
      this.roles = roles;
    } else {
      this.roles.clear();
      this.roles.addAll(roles);
    }
  }

  public ResponsibilitySet() {}

  public ResponsibilitySet(
    CodeSpace codeSpace,
    String privateCode,
    String name,
    Set<ResponsibilityRoleAssignment> roles
  ) {
    setCodeSpace(codeSpace);
    setPrivateCode(privateCode);
    this.name = name;
    this.roles = roles;
  }

  public ResponsibilityRoleAssignment getResponsibilityRoleAssignment(String id) {
    if (id != null && !CollectionUtils.isEmpty(roles)) {
      for (ResponsibilityRoleAssignment existingRole : roles) {
        if (id.equals(existingRole.getId())) {
          return existingRole;
        }
      }
    }
    throw new IllegalArgumentException(
      getClass().getSimpleName() + " with id: " + id + " not found"
    );
  }

  @PreRemove
  private void removeResponsibilitySetConnections() {
    if (roles != null) {
      roles.clear();
    }
  }
}
