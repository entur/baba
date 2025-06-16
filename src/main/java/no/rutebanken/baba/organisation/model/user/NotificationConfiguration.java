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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import no.rutebanken.baba.organisation.model.user.eventfilter.EventFilter;

@Entity
public class NotificationConfiguration {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_configuration_seq")
  @SequenceGenerator(
    name = "notification_configuration_seq",
    sequenceName = "notification_configuration_seq",
    allocationSize = 1
  )
  @JsonIgnore
  private Long pk;

  @Enumerated(EnumType.STRING)
  @NotNull
  private NotificationType notificationType;

  @OneToOne(cascade = CascadeType.ALL)
  @NotNull
  private EventFilter eventFilter;

  private boolean enabled;

  public NotificationType getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotificationType notificationType) {
    this.notificationType = notificationType;
  }

  public EventFilter getEventFilter() {
    return eventFilter;
  }

  public void setEventFilter(EventFilter eventFilter) {
    this.eventFilter = eventFilter;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
