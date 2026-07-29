package no.rutebanken.baba.organisation.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import no.rutebanken.baba.organisation.model.user.ContactDetails;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.entur.ror.permission.BabaUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();
  private static final String TEST_SUBJECT = "testSubject";
  private static final String TEST_ISSUER = "testIssuer";

  @AfterEach
  void restoreDefaultLocale() {
    Locale.setDefault(DEFAULT_LOCALE);
  }

  @Test
  void looksUpBabaUserByLowercasedEmailIndependentlyOfDefaultLocale() {
    // Turkish folds 'I' to the dotless 'ı', so a locale-sensitive toLowerCase would look up
    // an address that cannot match anything in the Baba database.
    Locale.setDefault(Locale.of("tr", "TR"));

    PermissionStoreUser permissionStoreUser = new PermissionStoreUser();
    permissionStoreUser.subject = TEST_SUBJECT;
    permissionStoreUser.email = "INGRID@example.org";

    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByEmail(any())).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(repository, subject -> permissionStoreUser, null);
    BabaUser babaUser = userService.getUserByAuthenticatedUser(authenticatedUser());

    verify(repository).getUserByEmail("ingrid@example.org");
    assertEquals("ingrid", babaUser.username);
  }

  private static User babaDatabaseUser() {
    User user = new User();
    user.setUsername("ingrid");
    user.setPrivateCode(TEST_SUBJECT);
    user.setContactDetails(new ContactDetails());
    return user;
  }

  private static AuthenticatedUser authenticatedUser() {
    return new AuthenticatedUser.AuthenticatedUserBuilder()
      .withSubject(TEST_SUBJECT)
      .withIssuer(TEST_ISSUER)
      .build();
  }
}
