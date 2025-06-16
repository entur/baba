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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * User defined event filter for job related events.
 */
@Entity
public class JobEventFilter extends EventFilter {

  @NotNull
  private String jobDomain;

  @NotNull
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> actions;

  @NotNull
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<JobState> states;

  public String getJobDomain() {
    return jobDomain;
  }

  public void setJobDomain(String jobDomain) {
    this.jobDomain = jobDomain;
  }

  public Set<String> getActions() {
    if (actions == null) {
      actions = new HashSet<>();
    }
    return actions;
  }

  public void setActions(Set<String> actions) {
    getActions().clear();
    getActions().addAll(actions);
  }

  public Set<JobState> getStates() {
    if (states == null) {
      states = new HashSet<>();
    }
    return states;
  }

  public void setStates(Set<JobState> states) {
    getStates().clear();
    getStates().addAll(states);
  }
}
