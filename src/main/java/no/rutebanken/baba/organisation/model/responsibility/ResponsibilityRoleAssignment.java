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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import org.springframework.util.CollectionUtils;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "rsp_role_assignment_unique_id",
      columnNames = { "code_space_pk", "privateCode", "entityVersion" }
    ),
  }
)
public class ResponsibilityRoleAssignment extends CodeSpaceEntity {

  @NotNull
  @ManyToOne
  private Role typeOfResponsibilityRole;

  @NotNull
  @ManyToOne
  private Organisation responsibleOrganisation;

  @ManyToOne
  private AdministrativeZone responsibleArea;

  @OneToMany(
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER,
    mappedBy = "responsibilityRoleAssignment"
  )
  private Set<EntityClassificationAssignment> responsibleEntityClassifications;

  public Role getTypeOfResponsibilityRole() {
    return typeOfResponsibilityRole;
  }

  public void setTypeOfResponsibilityRole(Role typeOfResponsibilityRole) {
    this.typeOfResponsibilityRole = typeOfResponsibilityRole;
  }

  public Organisation getResponsibleOrganisation() {
    return responsibleOrganisation;
  }

  public void setResponsibleOrganisation(Organisation responsibleOrganisation) {
    this.responsibleOrganisation = responsibleOrganisation;
  }

  public AdministrativeZone getResponsibleArea() {
    return responsibleArea;
  }

  public void setResponsibleArea(AdministrativeZone responsibleArea) {
    this.responsibleArea = responsibleArea;
  }

  public Set<EntityClassificationAssignment> getResponsibleEntityClassifications() {
    if (responsibleEntityClassifications == null) {
      this.responsibleEntityClassifications = new HashSet<>();
    }
    return responsibleEntityClassifications;
  }

  public EntityClassificationAssignment getResponsibleEntityClassification(
    String entityClassificationId
  ) {
    if (
      entityClassificationId != null && !CollectionUtils.isEmpty(responsibleEntityClassifications)
    ) {
      for (EntityClassificationAssignment existingClassificationAssignment : responsibleEntityClassifications) {
        if (
          entityClassificationId.equals(
            existingClassificationAssignment.getEntityClassification().getId()
          )
        ) {
          return existingClassificationAssignment;
        }
      }
    }
    return null;
  }

  public void setResponsibleEntityClassifications(
    Set<EntityClassificationAssignment> responsibleEntityClassifications
  ) {
    getResponsibleEntityClassifications().clear();
    getResponsibleEntityClassifications().addAll(responsibleEntityClassifications);
  }

  @PreRemove
  private void removeResponsibilitySetConnections() {
    getResponsibleEntityClassifications().clear();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final ResponsibilityRoleAssignment target = new ResponsibilityRoleAssignment();

    public Builder withPrivateCode(String privateCode) {
      target.setPrivateCode(privateCode);
      return this;
    }

    public Builder withCodeSpace(CodeSpace codeSpace) {
      target.setCodeSpace(codeSpace);
      return this;
    }

    public Builder withResponsibleOrganisation(Organisation responsibleOrganisation) {
      target.setResponsibleOrganisation(responsibleOrganisation);
      return this;
    }

    public Builder withResponsibleArea(AdministrativeZone responsibleArea) {
      target.setResponsibleArea(responsibleArea);
      return this;
    }

    public Builder withTypeOfResponsibilityRole(Role typeOfResponsibilityRole) {
      target.setTypeOfResponsibilityRole(typeOfResponsibilityRole);
      return this;
    }

    public Builder withResponsibleEntityClassifications(
      Set<EntityClassificationAssignment> responsibleEntityClassifications
    ) {
      target.setResponsibleEntityClassifications(responsibleEntityClassifications);
      return this;
    }

    public ResponsibilityRoleAssignment build() {
      return target;
    }
  }
}
