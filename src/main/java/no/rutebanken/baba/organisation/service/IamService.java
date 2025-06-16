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

package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;

/**
 * Update the state of a remote IAM service to keep it in sync with the Baba user database.
 */
public interface IamService {
  void createUser(User user);

  void updateUser(User user);

  void resetPassword(User user);

  void removeUser(User user);

  void createRole(Role role);

  void removeRole(Role role);

  void updateResponsibilitySet(ResponsibilitySet responsibilitySet);

  boolean hasUserWithEmail(String email);

  String getUserWithEmail(String email);
}
