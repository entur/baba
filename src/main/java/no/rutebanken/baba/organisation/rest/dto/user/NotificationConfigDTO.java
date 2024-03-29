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

package no.rutebanken.baba.organisation.rest.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import no.rutebanken.baba.organisation.model.user.NotificationType;
@Schema(description = "Configuration of a single type of notifications for a user. Combines an event filter describing which " +
                          "events should be notified with a notification type describing how they should be notified")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationConfigDTO {

    public NotificationType notificationType;

    public EventFilterDTO eventFilter;

    public boolean enabled = true;

    public NotificationConfigDTO(NotificationType notificationType, boolean enabled, EventFilterDTO eventFilter) {
        this.notificationType = notificationType;
        this.enabled = enabled;
        this.eventFilter = eventFilter;
    }

    public NotificationConfigDTO() {
    }
}
