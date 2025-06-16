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

package no.rutebanken.baba.organisation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@MappedSuperclass
public abstract class VersionedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "versioned_entity_seq")
  @SequenceGenerator(
    name = "versioned_entity_seq",
    sequenceName = "versioned_entity_seq",
    allocationSize = 1
  )
  @JsonIgnore
  private Long pk;

  @Version
  @JsonIgnore
  @NotNull
  // Version for optimistic locking
  private Long lockVersion = 1L;

  @NotNull
  // Publicly exposed version of entity
  private Long entityVersion = 1L;

  @JsonIgnore
  public String getType() {
    return getClass().getSimpleName();
  }

  @NotNull
  private String privateCode;

  public String getPrivateCode() {
    return privateCode;
  }

  public void setPrivateCode(String privateCode) {
    this.privateCode = privateCode;
  }

  public String getId() {
    return getPrivateCode();
  }

  public Long getPk() {
    return pk;
  }

  public void setPk(Long pk) {
    this.pk = pk;
  }

  public Long getLockVersion() {
    return lockVersion;
  }

  public void setLockVersion(Long lockVersion) {
    this.lockVersion = lockVersion;
  }

  public Long getEntityVersion() {
    return entityVersion;
  }

  public void setEntityVersion(Long entityVersion) {
    this.entityVersion = entityVersion;
  }
}
