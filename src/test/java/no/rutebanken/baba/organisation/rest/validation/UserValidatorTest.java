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

import no.rutebanken.baba.organisation.rest.dto.user.ContactDetailsDTO;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserValidatorTest {

  private final UserValidator userValidator = new UserValidator();

  @Test
  void validateCreateMinimalOk() {
    userValidator.validateCreate(minimalUser());
  }

  @Test
  void validateCreateWithCapitalAndNumberAllowed() {
    UserDTO user = minimalUser();
    user.username = "userNo1";
    userValidator.validateCreate(user);
  }

  @Test
  void validateCreateWithDotAllowed() {
    UserDTO user = minimalUser();
    user.username = "user.No1";
    userValidator.validateCreate(user);
  }

  @Test
  void validateCreateWithInvalidUsernameFails() {
    UserDTO user = minimalUser();
    user.username = "user 1";
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
  }

  @Test
  void validateCreateWithoutOrganisationFails() {
    UserDTO user = minimalUser();
    user.organisationRef = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
  }

  @Test
  void validateCreateWithoutContactDetailsFails() {
    UserDTO user = minimalUser();
    user.contactDetails = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
  }

  @Test
  void validateCreateWithoutEmailFails() {
    UserDTO user = minimalUser();
    user.contactDetails.email = null;
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
  }

  @Test
  void validateUpdateMinimalUserOK() {
    userValidator.validateUpdate(minimalUser(), null);
  }

  @Test
  void validateInvalidEmailFails() {
    UserDTO user = minimalUser();
    user.contactDetails = new ContactDetailsDTO("first", "last", "34234", "illegalEmail");
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
  }

  @Test
  void validateValidEmailOK() {
    UserDTO user = minimalUser();
    user.contactDetails = new ContactDetailsDTO("first", "last", "34234", "legal@email.com");
    userValidator.validateCreate(user);
  }

  protected UserDTO minimalUser() {
    UserDTO userDTO = new UserDTO();
    userDTO.username = "username";
    userDTO.organisationRef = "organisation";
    userDTO.contactDetails = new ContactDetailsDTO(null, null, null, "valid@email.org");

    return userDTO;
  }
}
