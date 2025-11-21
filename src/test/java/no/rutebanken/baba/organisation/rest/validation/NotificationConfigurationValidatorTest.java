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

import java.util.HashSet;
import java.util.Set;
import no.rutebanken.baba.organisation.model.user.NotificationType;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobState;
import no.rutebanken.baba.organisation.rest.dto.user.EventFilterDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationConfigurationValidatorTest {

  private final NotificationConfigurationValidator validator =
    new NotificationConfigurationValidator();

  @Test
  void validateWithoutUserNameFails() {
    Set<NotificationConfigDTO> config = withCrudFilter();
    Assertions.assertThrows(IllegalArgumentException.class, () -> validator.validate(null, config));
  }

  @Test
  void validateNotificationWithoutEventFilterFails() {
    Set<NotificationConfigDTO> config = withCrudFilter();
    config.iterator().next().eventFilter = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateNotificationWithoutNotificationTypeFails() {
    Set<NotificationConfigDTO> config = withCrudFilter();
    config.iterator().next().notificationType = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateCrudFilterWithoutEntityClassificationsFails() {
    Set<NotificationConfigDTO> config = withCrudFilter();
    config.iterator().next().eventFilter.entityClassificationRefs = Set.of();
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateJobFilterWithoutJobDomainFails() {
    Set<NotificationConfigDTO> config = withJobFilter();
    config.iterator().next().eventFilter.jobDomain = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateJobFilterWithNullActionFails() {
    Set<NotificationConfigDTO> config = withJobFilter();
    config.iterator().next().eventFilter.actions = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateJobFilterWithoutActionFails() {
    Set<NotificationConfigDTO> config = withJobFilter();
    config.iterator().next().eventFilter.actions = new HashSet<>();
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateJobFilterWithNullStatenFails() {
    Set<NotificationConfigDTO> config = withJobFilter();
    config.iterator().next().eventFilter.states = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  @Test
  void validateJobFilterWithoutStatenFails() {
    Set<NotificationConfigDTO> config = withJobFilter();
    config.iterator().next().eventFilter.states = Set.of();
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> validator.validate("user", config)
    );
  }

  protected Set<NotificationConfigDTO> withCrudFilter() {
    NotificationConfigDTO configDTO = new NotificationConfigDTO();
    configDTO.notificationType = NotificationType.EMAIL;

    EventFilterDTO eventFilter = new EventFilterDTO(EventFilterDTO.EventFilterType.CRUD);
    eventFilter.entityClassificationRefs = Set.of("ref1");
    configDTO.eventFilter = eventFilter;

    return Set.of(configDTO);
  }

  protected Set<NotificationConfigDTO> withJobFilter() {
    NotificationConfigDTO configDTO = new NotificationConfigDTO();
    configDTO.notificationType = NotificationType.EMAIL;

    EventFilterDTO eventFilter = new EventFilterDTO(EventFilterDTO.EventFilterType.JOB);
    eventFilter.actions = Set.of("BUILD");
    eventFilter.jobDomain = EventFilterDTO.JobDomain.GEOCODER;
    eventFilter.states = Set.of(JobState.FAILED);
    configDTO.eventFilter = eventFilter;

    return Set.of(configDTO);
  }
}
