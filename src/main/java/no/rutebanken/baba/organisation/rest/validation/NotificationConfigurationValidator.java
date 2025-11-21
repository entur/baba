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

package no.rutebanken.baba.organisation.rest.validation;

import java.util.Set;
import no.rutebanken.baba.organisation.rest.dto.user.EventFilterDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class NotificationConfigurationValidator {

  public void validate(String userName, Set<NotificationConfigDTO> config) {
    Assert.notNull(userName, "userName required");
    if (config != null) {
      for (NotificationConfigDTO notificationConfigDTO : config) {
        Assert.notNull(
          notificationConfigDTO.notificationType,
          "notifications.notificationType required"
        );
        assertEventFilter(notificationConfigDTO.eventFilter);
      }
    }
  }

  private void assertEventFilter(EventFilterDTO eventFilter) {
    Assert.notNull(eventFilter, "notifications.eventFilter required");
    Assert.notNull(eventFilter.type, "notifications.eventFilter.type required");

    if (eventFilter.type == EventFilterDTO.EventFilterType.JOB) {
      Assert.notNull(
        eventFilter.jobDomain,
        "notifications.eventFilter.jobDomain required for JOB event filter"
      );
      Assert.notEmpty(
        eventFilter.states,
        "notifications.eventFilter.states required for JOB event filter"
      );
      Assert.notEmpty(
        eventFilter.actions,
        "notifications.eventFilter.actions required for JOB event filter"
      );
    } else if (eventFilter.type == EventFilterDTO.EventFilterType.CRUD) {
      Assert.notEmpty(
        eventFilter.entityClassificationRefs,
        "notifications.eventFilter.entityClassificationRefs required for CRUD event filter"
      );
    }
  }
}
