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

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class UserValidator implements DTOValidator<User, UserDTO> {

  // mirrored in ninkasi/src/utils/usernameValidation.ts
  private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-[.]]{3,30}$";
  private static final String USERNAME_RULE =
    "3-30 characters using only letters, digits, underscore, hyphen and dot";

  @Override
  public void validateCreate(UserDTO dto) {
    Assert.hasLength(dto.username, "username required");
    Assert.isTrue(
      dto.username.matches(USERNAME_PATTERN),
      () ->
        "username '%s' (%d characters) must be %s".formatted(
            dto.username,
            dto.username.codePointCount(0, dto.username.length()),
            USERNAME_RULE
          )
    );
    Assert.hasLength(dto.organisationRef, "organisationRef required");
    assertCommon(dto);
  }

  @Override
  public void validateUpdate(UserDTO dto, User entity) {
    assertCommon(dto);
  }

  private void assertCommon(UserDTO dto) {
    Assert.notNull(dto.contactDetails, "contactDetails required");
    Assert.notNull(dto.contactDetails.email, "contactDetails.email required");
    Assert.isTrue(
      isValidEmailAddress(dto.contactDetails.email),
      "contactDetails.email must be a valid email address"
    );
  }

  public static boolean isValidEmailAddress(String email) {
    boolean result = true;
    try {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
    } catch (AddressException ex) {
      result = false;
    }
    return result;
  }
}
