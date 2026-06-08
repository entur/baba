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

package no.rutebanken.baba.organisation.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.model.user.NotificationConfiguration;
import no.rutebanken.baba.organisation.model.user.NotificationType;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobEventFilter;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobState;
import no.rutebanken.baba.organisation.rest.dto.user.EventFilterDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.junit.jupiter.api.Test;

class NotificationConfigurationMapperTest {

  private final NotificationConfigurationMapper mapper = new NotificationConfigurationMapper(
    null,
    null,
    null,
    null
  );

  @Test
  void omitsConfigurationsScopedToRetiredJobDomain() {
    // A stored filter on the retired GEOCODER domain can no longer be parsed to the
    // JobDomain enum. The read path must omit it rather than fail, while keeping the
    // user's other configurations.
    NotificationConfiguration geocoder = jobConfiguration("GEOCODER");
    NotificationConfiguration timetable = jobConfiguration("TIMETABLE");

    Set<NotificationConfigDTO> dtos = mapper.toDTO(List.of(geocoder, timetable), false);

    assertEquals(1, dtos.size(), "Configuration on the retired GEOCODER domain should be omitted");
    assertEquals(
      EventFilterDTO.JobDomain.TIMETABLE,
      dtos.iterator().next().eventFilter.jobDomain,
      "The surviving configuration should be the TIMETABLE one"
    );
  }

  @Test
  void mapsKnownJobDomain() {
    Set<NotificationConfigDTO> dtos = mapper.toDTO(List.of(jobConfiguration("TIMETABLE")), false);

    assertEquals(1, dtos.size());
    assertEquals(EventFilterDTO.JobDomain.TIMETABLE, dtos.iterator().next().eventFilter.jobDomain);
  }

  private static NotificationConfiguration jobConfiguration(String jobDomain) {
    JobEventFilter eventFilter = new JobEventFilter();
    eventFilter.setJobDomain(jobDomain);
    eventFilter.setActions(Set.of("BUILD"));
    eventFilter.setStates(Set.of(JobState.FAILED));

    NotificationConfiguration configuration = new NotificationConfiguration();
    configuration.setNotificationType(NotificationType.EMAIL);
    configuration.setEnabled(true);
    configuration.setEventFilter(eventFilter);
    return configuration;
  }
}
