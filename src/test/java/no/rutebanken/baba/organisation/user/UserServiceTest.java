package no.rutebanken.baba.organisation.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import java.util.Locale;
import no.rutebanken.baba.organisation.model.user.ContactDetails;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUnavailableException;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.entur.ror.permission.BabaUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

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

  @Test
  void rejectsUserUnknownToPermissionStoreWithoutFallingBackToBabaDatabase() {
    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByPrivateCode(TEST_SUBJECT)).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(repository, subject -> null, null);
    AuthenticatedUser authenticatedUser = authenticatedUser();

    assertThrows(
      NotFoundException.class,
      () -> userService.getUserByAuthenticatedUser(authenticatedUser)
    );
    verify(repository, never()).getUserByPrivateCode(any());
  }

  @Test
  void fallsBackToBabaDatabaseWhenPermissionStoreIsUnavailable() {
    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByPrivateCode(TEST_SUBJECT)).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(
      repository,
      subject -> {
        throw new PermissionStoreUnavailableException("Permission store unavailable", null);
      },
      null
    );
    BabaUser babaUser = userService.getUserByAuthenticatedUser(authenticatedUser());

    assertEquals("ingrid", babaUser.username);
    verify(repository).getUserByPrivateCode(TEST_SUBJECT);
  }

  @Test
  void rejectsSubjectMatchingSeveralPermissionStoreUsersWithoutFallingBackToBabaDatabase() {
    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByPrivateCode(TEST_SUBJECT)).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(
      repository,
      subject -> {
        throw new IllegalStateException("Multiple users found for subject " + subject);
      },
      null
    );
    AuthenticatedUser authenticatedUser = authenticatedUser();

    assertThrows(
      IllegalStateException.class,
      () -> userService.getUserByAuthenticatedUser(authenticatedUser)
    );
    verify(repository, never()).getUserByPrivateCode(any());
  }

  @Test
  void rejectsPermissionStoreUserWithoutEmailWithoutFallingBackToBabaDatabase() {
    PermissionStoreUser permissionStoreUser = new PermissionStoreUser();
    permissionStoreUser.subject = TEST_SUBJECT;

    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByPrivateCode(TEST_SUBJECT)).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(repository, subject -> permissionStoreUser, null);
    AuthenticatedUser authenticatedUser = authenticatedUser();

    assertThrows(
      ServerErrorException.class,
      () -> userService.getUserByAuthenticatedUser(authenticatedUser)
    );
    verify(repository, never()).getUserByPrivateCode(any());
  }

  @Test
  void doesNotFallBackToBabaDatabaseWhenTheLookupByEmailFails() {
    PermissionStoreUser permissionStoreUser = new PermissionStoreUser();
    permissionStoreUser.subject = TEST_SUBJECT;
    permissionStoreUser.email = "ingrid@example.org";

    UserRepository repository = mock(UserRepository.class);
    when(repository.getUserByEmail(any()))
      .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
    when(repository.getUserByPrivateCode(TEST_SUBJECT)).thenReturn(babaDatabaseUser());

    UserService userService = new UserService(repository, subject -> permissionStoreUser, null);
    AuthenticatedUser authenticatedUser = authenticatedUser();

    assertThrows(
      IncorrectResultSizeDataAccessException.class,
      () -> userService.getUserByAuthenticatedUser(authenticatedUser)
    );
    verify(repository, never()).getUserByPrivateCode(any());
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
