package no.rutebanken.baba.organisation.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.rest.dto.user.ContactDetailsDTO;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  private final UserMapper mapper = new UserMapper(null, null, null, null, null);

  @AfterEach
  void restoreDefaultLocale() {
    Locale.setDefault(DEFAULT_LOCALE);
  }

  @Test
  void lowercasesUsernameAndEmailIndependentlyOfDefaultLocale() {
    // Turkish folds 'I' to the dotless 'ı', so a locale-sensitive toLowerCase would corrupt these.
    Locale.setDefault(Locale.of("tr", "TR"));

    UserDTO dto = new UserDTO();
    dto.username = "INGRID";
    dto.contactDetails =
      new ContactDetailsDTO("Ingrid", "Nilsen", "12345678", "INGRID@example.org");

    User user = mapper.createFromDTO(dto, User.class);

    assertEquals("ingrid", user.getUsername());
    assertEquals("ingrid@example.org", user.getContactDetails().getEmail());
  }

  @Test
  void mapsContactDetailsWithoutEmail() {
    UserDTO dto = new UserDTO();
    dto.username = "ingrid";
    dto.contactDetails = new ContactDetailsDTO("Ingrid", "Nilsen", "12345678", null);

    User user = mapper.createFromDTO(dto, User.class);

    assertNull(user.getContactDetails().getEmail());
    assertEquals("Ingrid", user.getContactDetails().getFirstName());
  }
}
