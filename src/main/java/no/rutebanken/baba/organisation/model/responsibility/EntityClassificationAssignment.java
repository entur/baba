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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class EntityClassificationAssignment {

  @Id
  @GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "entity_classification_assignment_seq"
  )
  @SequenceGenerator(
    name = "entity_classification_assignment_seq",
    sequenceName = "entity_classification_assignment_seq",
    allocationSize = 1
  )
  @JsonIgnore
  private Long pk;

  @ManyToOne
  @NotNull
  private EntityClassification entityClassification;

  @ManyToOne
  private ResponsibilityRoleAssignment responsibilityRoleAssignment;

  private boolean allow = true;

  public EntityClassificationAssignment() {}

  public EntityClassificationAssignment(
    EntityClassification entityClassification,
    ResponsibilityRoleAssignment responsibilityRoleAssignment,
    boolean allow
  ) {
    this.entityClassification = entityClassification;
    this.responsibilityRoleAssignment = responsibilityRoleAssignment;
    this.allow = allow;
  }

  public EntityClassification getEntityClassification() {
    return entityClassification;
  }

  public void setEntityClassification(EntityClassification entityClassification) {
    this.entityClassification = entityClassification;
  }

  public boolean isAllow() {
    return allow;
  }

  public void setAllow(boolean allow) {
    this.allow = allow;
  }

  public ResponsibilityRoleAssignment getResponsibilityRoleAssignment() {
    return responsibilityRoleAssignment;
  }

  public void setResponsibilityRoleAssignment(
    ResponsibilityRoleAssignment responsibilityRoleAssignment
  ) {
    this.responsibilityRoleAssignment = responsibilityRoleAssignment;
  }

  @Override
  public String toString() {
    return (
      "EntityClassificationAssignment{" +
      "entityClassification=" +
      entityClassification +
      ", allow=" +
      allow +
      '}'
    );
  }
}
