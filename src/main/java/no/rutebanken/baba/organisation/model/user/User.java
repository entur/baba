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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import javax.jdo.annotations.Unique;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;

@Entity
@Table(
  name = "user_account",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "user_unique_username",
      columnNames = { "privateCode", "entityVersion" }
    ),
  }
)
public class User extends VersionedEntity {

  @NotNull
  @Unique
  private String username;

  private boolean personalAccount = true;

  @NotNull
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ContactDetails contactDetails;

  @NotNull
  @ManyToOne
  private Organisation organisation;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Set<NotificationConfiguration> notificationConfigurations;

  @ManyToMany
  private Set<ResponsibilitySet> responsibilitySets;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @NotNull
  public ContactDetails getContactDetails() {
    return contactDetails;
  }

  public void setContactDetails(ContactDetails contactDetails) {
    this.contactDetails = contactDetails;
  }

  public Set<NotificationConfiguration> getNotificationConfigurations() {
    if (notificationConfigurations == null) {
      notificationConfigurations = new HashSet<>();
    }
    return notificationConfigurations;
  }

  public void setNotificationConfigurations(
    Set<NotificationConfiguration> notificationConfigurations
  ) {
    getNotificationConfigurations().clear();
    getNotificationConfigurations().addAll(notificationConfigurations);
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
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
    getNotificationConfigurations().clear();
  }

  @Override
  public String getId() {
    return String.join(":", getType(), getPrivateCode());
  }

  public static User.Builder builder() {
    return new User.Builder();
  }

  /**
   *
   * @return true if the user/password account, false if the account is used only for sending notifications
   */
  public boolean isPersonalAccount() {
    return personalAccount;
  }

  public User setPersonalAccount(boolean userAccount) {
    this.personalAccount = userAccount;
    return this;
  }

  public static class Builder {

    private final User user = new User();

    public Builder withPrivateCode(String privateCode) {
      user.setPrivateCode(privateCode);
      return this;
    }

    public Builder withUsername(String username) {
      user.setUsername(username);
      return this;
    }

    public Builder withOrganisation(Organisation organisation) {
      user.setOrganisation(organisation);
      return this;
    }

    public Builder withNotifications(Set<NotificationConfiguration> notificationConfigurations) {
      user.setNotificationConfigurations(notificationConfigurations);
      return this;
    }

    public Builder withResponsibilitySets(Set<ResponsibilitySet> responsibilitySets) {
      user.setResponsibilitySets(responsibilitySets);
      return this;
    }

    public Builder withContactDetails(ContactDetails contactDetails) {
      user.setContactDetails(contactDetails);
      return this;
    }

    public User build() {
      return user;
    }
  }
}
