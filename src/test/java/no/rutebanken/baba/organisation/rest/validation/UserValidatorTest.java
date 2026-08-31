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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.rutebanken.baba.organisation.rest.dto.user.ContactDetailsDTO;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
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
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
  }

  @Test
  void validateCreateWithTooLongUsernameFailsWithDescriptiveMessage() {
    UserDTO user = minimalUser();
    user.username = "a".repeat(31);
    IllegalArgumentException e = assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
    assertTrue(e.getMessage().contains("31 characters"), e.getMessage());
  }

  @Test
  void validateCreateReportsCharacterCountNotUtf16Units() {
    UserDTO user = minimalUser();
    user.username = "ab\uD83D\uDE00";
    IllegalArgumentException e = assertThrows(
      IllegalArgumentException.class,
      () -> userValidator.validateCreate(user)
    );
    assertTrue(e.getMessage().contains("(3 characters)"), e.getMessage());
  }

  @Test
  void validateCreateWithUsernameAtLengthBoundariesOk() {
    UserDTO user = minimalUser();
    user.username = "abc";
    userValidator.validateCreate(user);
    user.username = "a".repeat(30);
    userValidator.validateCreate(user);
  }

  @Test
  void validateCreateWithTooShortUsernameFails() {
    UserDTO user = minimalUser();
    user.username = "ab";
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
  }

  @Test
  void validateCreateWithoutOrganisationFails() {
    UserDTO user = minimalUser();
    user.organisationRef = null;
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
  }

  @Test
  void validateCreateWithoutContactDetailsFails() {
    UserDTO user = minimalUser();
    user.contactDetails = null;
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
  }

  @Test
  void validateCreateWithoutEmailFails() {
    UserDTO user = minimalUser();
    user.contactDetails.email = null;
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
  }

  @Test
  void validateUpdateMinimalUserOK() {
    userValidator.validateUpdate(minimalUser(), null);
  }

  @Test
  void validateInvalidEmailFails() {
    UserDTO user = minimalUser();
    user.contactDetails = new ContactDetailsDTO("first", "last", "34234", "illegalEmail");
    assertThrows(IllegalArgumentException.class, () -> userValidator.validateCreate(user));
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
