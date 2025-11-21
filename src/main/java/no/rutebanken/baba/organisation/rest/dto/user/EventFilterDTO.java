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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobState;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityClassificationDTO;

@Schema(description = "Filter describing events of interest")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventFilterDTO {

  public enum JobDomain {
    TIMETABLE,
    GEOCODER,
    GRAPH,
    TIAMAT,
    TIMETABLE_PUBLISH,
  }

  public enum EventFilterType {
    JOB,
    CRUD,
  }

  public EventFilterType type;

  public String organisationRef;

  // Job event filter values
  public JobDomain jobDomain;

  public Set<String> actions;

  public Set<JobState> states;

  // Crud event filter values
  public Set<String> administrativeZoneRefs;

  public Set<String> entityClassificationRefs;

  @Schema(
    description = "Fully mapped object for ease of use. Disregarded in CRUD operations (use reference)"
  )
  public Set<EntityClassificationDTO> entityClassifications = new HashSet<>();

  @Schema(
    description = "Fully mapped object for ease of use. Disregarded in CRUD operations (use reference)"
  )
  public OrganisationDTO organisation;

  public EventFilterDTO(EventFilterType type) {
    this.type = type;
  }

  public EventFilterDTO() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventFilterDTO that = (EventFilterDTO) o;

    if (type != that.type) {
      return false;
    }
    if (!Objects.equals(organisationRef, that.organisationRef)) {
      return false;
    }
    if (jobDomain != that.jobDomain) {
      return false;
    }
    if (!Objects.equals(actions, that.actions)) {
      return false;
    }
    if (!Objects.equals(states, that.states)) {
      return false;
    }
    if (!Objects.equals(administrativeZoneRefs, that.administrativeZoneRefs)) {
      return false;
    }
    return Objects.equals(entityClassificationRefs, that.entityClassificationRefs);
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (organisationRef != null ? organisationRef.hashCode() : 0);
    result = 31 * result + (jobDomain != null ? jobDomain.hashCode() : 0);
    result = 31 * result + (actions != null ? actions.hashCode() : 0);
    result = 31 * result + (states != null ? states.hashCode() : 0);
    result = 31 * result + (administrativeZoneRefs != null ? administrativeZoneRefs.hashCode() : 0);
    result =
      31 * result + (entityClassificationRefs != null ? entityClassificationRefs.hashCode() : 0);
    return result;
  }
}
