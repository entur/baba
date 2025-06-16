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

package no.rutebanken.baba.organisation.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import no.rutebanken.baba.organisation.TestConstantsOrganisation;
import no.rutebanken.baba.organisation.model.user.NotificationType;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobState;
import no.rutebanken.baba.organisation.repository.BaseIntegrationTest;
import no.rutebanken.baba.organisation.rest.dto.user.EventFilterDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

class NotificationConfigurationResourceTest extends BaseIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private static final String PATH = "/services/organisations/users";

  private String url(String userName) {
    return PATH + "/" + userName + "/" + "notification_configurations";
  }

  @Test
  void userNotFound() {
    ResponseEntity<String> entity = restTemplate.getForEntity(url("unknownUser"), String.class);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  @Test
  void crudNotificationConfig() {
    String url = url(TestConstantsOrganisation.USER_USERNAME);

    Set<NotificationConfigDTO> config = Set.of(
      new NotificationConfigDTO(NotificationType.EMAIL, true, crudEventFilter()),
      new NotificationConfigDTO(NotificationType.WEB, false, jobEventFilter())
    );
    restTemplate.put(url, config, String.class);
    assertConfig(config);

    Set<NotificationConfigDTO> updateConfig = Set.of(
      new NotificationConfigDTO(NotificationType.EMAIL, true, jobEventFilter())
    );
    restTemplate.put(url, updateConfig);
    assertConfig(updateConfig);

    restTemplate.delete(url);

    ResponseEntity<NotificationConfigDTO[]> entity = restTemplate.getForEntity(
      url,
      NotificationConfigDTO[].class
    );
    assertEquals(0, entity.getBody().length);
  }

  @Test
  void createInvalidNotificationConfig() {
    String url = url(TestConstantsOrganisation.USER_USERNAME);
    Set<NotificationConfigDTO> inConfig = Set.of(
      new NotificationConfigDTO(null, true, jobEventFilter())
    );
    ResponseEntity<Void> exchange = restTemplate.exchange(
      url,
      HttpMethod.PUT,
      new HttpEntity<>(inConfig),
      Void.class
    );
    assertEquals(HttpStatus.BAD_REQUEST, exchange.getStatusCode());
  }

  private EventFilterDTO crudEventFilter() {
    EventFilterDTO eventFilterDTO = new EventFilterDTO(EventFilterDTO.EventFilterType.CRUD);
    eventFilterDTO.entityClassificationRefs =
      Set.of(TestConstantsOrganisation.ENTITY_CLASSIFICATION_ID);

    eventFilterDTO.administrativeZoneRefs =
      Set.copyOf(ResourceTestUtils.addAdminZones(restTemplate, "z1", "z2"));
    return eventFilterDTO;
  }

  private EventFilterDTO jobEventFilter() {
    EventFilterDTO eventFilterDTO = new EventFilterDTO(EventFilterDTO.EventFilterType.JOB);
    eventFilterDTO.organisationRef = TestConstantsOrganisation.ORGANISATION_ID;
    eventFilterDTO.actions = Set.of("VALIDATION_LEVEL_1");
    eventFilterDTO.jobDomain = EventFilterDTO.JobDomain.TIMETABLE;
    eventFilterDTO.states = Set.of(JobState.FAILED);
    return eventFilterDTO;
  }

  protected void assertConfig(Set<NotificationConfigDTO> inConfig) {
    ResponseEntity<NotificationConfigDTO[]> rsp = restTemplate.getForEntity(
      url(TestConstantsOrganisation.USER_USERNAME),
      NotificationConfigDTO[].class
    );
    Set<NotificationConfigDTO> outConfig = Set.of(rsp.getBody());

    if (CollectionUtils.isEmpty(inConfig)) {
      Assertions.assertTrue(CollectionUtils.isEmpty(outConfig));
    } else {
      assertEquals(inConfig.size(), outConfig.size());
      for (NotificationConfigDTO in : inConfig) {
        Assertions.assertTrue(outConfig.stream().anyMatch(out -> isEqual(in, out)));
      }
    }
  }

  private boolean isEqual(NotificationConfigDTO in, NotificationConfigDTO out) {
    return in.notificationType == out.notificationType && isEqual(in.eventFilter, out.eventFilter);
  }

  private boolean isEqual(EventFilterDTO in, EventFilterDTO out) {
    return in.equals(out);
  }
}
