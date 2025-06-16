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

package no.rutebanken.baba.organisation.model.user.eventfilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import no.rutebanken.baba.organisation.model.organisation.Organisation;

/**
 * User defined filter for events.
 */
@Entity
public abstract class EventFilter {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_filter_seq")
  @SequenceGenerator(
    name = "event_filter_seq",
    sequenceName = "event_filter_seq",
    allocationSize = 1
  )
  @JsonIgnore
  private Long pk;

  @ManyToOne
  private Organisation organisation;

  public Organisation getOrganisation() {
    return organisation;
  }

  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }
}
