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

package no.rutebanken.baba.organisation.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.jdo.annotations.Unique;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;

/**
 * An M2M client identified by its client-id (private code) and display name (name)
 */
@Entity
@Table(
  name = "m2m_client",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "m2m_client_unique_client_id",
      columnNames = { "privateCode", "entityVersion" }
    ),
  }
)
public class M2MClient extends VersionedEntity {

  public static final String INTERNAL_ISSUER = "Internal";
  public static final String PARTNER_ISSUER = "Partner";

  @NotNull
  @Unique
  private String name;

  @ManyToMany
  private Set<ResponsibilitySet> responsibilitySets;

  private Long enturOrganisationId;

  private String issuer;

  public String getName() {
    return name;
  }

  public void setName(String username) {
    this.name = username;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public Long getEnturOrganisationId() {
    return enturOrganisationId;
  }

  public void setEnturOrganisationId(Long enturOrganisationId) {
    this.enturOrganisationId = enturOrganisationId;
  }

  public Set<ResponsibilitySet> getResponsibilitySets() {
    if (responsibilitySets == null) {
      this.responsibilitySets = new HashSet<>();
    }
    return responsibilitySets;
  }

  public void setResponsibilitySets(Set<ResponsibilitySet> responsibilitySets) {
    getResponsibilitySets().clear();
    getResponsibilitySets().addAll(responsibilitySets);
  }

  @PreRemove
  private void removeChildren() {
    getResponsibilitySets().clear();
  }

  @Override
  public String getId() {
    return String.join(":", getType(), getPrivateCode());
  }

  public boolean isInternal() {
    return INTERNAL_ISSUER.equals(getIssuer());
  }

  public boolean isPartner() {
    return PARTNER_ISSUER.equals(getIssuer());
  }
}
