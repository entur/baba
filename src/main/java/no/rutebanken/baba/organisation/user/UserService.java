package no.rutebanken.baba.organisation.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.rutebanken.baba.exceptions.BabaException;
import no.rutebanken.baba.organisation.email.NewUserEmailSender;
import no.rutebanken.baba.organisation.m2m.EnturClientM2MRoleAssignmentRepository;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.service.IamService;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import no.rutebanken.baba.security.permissionstore.CodespaceMapping;
import no.rutebanken.baba.security.permissionstore.OrganisationRegisterClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.entur.ror.permission.BabaContactDetails;
import org.entur.ror.permission.BabaUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private final UserRepository repository;
  private final PermissionStoreClient permissionStoreClient;
  private final OrganisationRegisterClient organisationRegisterClient;
  private final IamService iamService;
  private final NewUserEmailSender newUserEmailSender;
  private final EnturClientM2MRoleAssignmentRepository enturClientM2MRoleAssignmentRepository;

  public UserService(
    UserRepository repository,
    PermissionStoreClient permissionStoreClient,
    OrganisationRegisterClient organisationRegisterClient,
    EnturClientM2MRoleAssignmentRepository enturClientM2MRoleAssignmentRepository,
    IamService iamService,
    NewUserEmailSender newUserEmailSender
  ) {
    this.repository = repository;
    this.permissionStoreClient = permissionStoreClient;
    this.organisationRegisterClient = organisationRegisterClient;
    this.iamService = iamService;
    this.newUserEmailSender = newUserEmailSender;
    this.enturClientM2MRoleAssignmentRepository = enturClientM2MRoleAssignmentRepository;
  }

  /**
   * Return details about an authenticated user.
   * The user can either represent a user account from the Baba database or a machine-to-machine client.
   * @throws NotFoundException if the authenticated user cannot be found.
   */
  public BabaUser getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
    if (authenticatedUser.isClient()) {
      BabaUser babaUser = new BabaUser();
      babaUser.isClient = true;
      babaUser.username = enturClientM2MRoleAssignmentRepository.getClientName(authenticatedUser);
      return babaUser;
    } else if (authenticatedUser.isRor()) {
      User user = repository.getUserByUsername(authenticatedUser.username());
      return mapBabaUser(user);
    } else {
      User user = permissionStoreUser(authenticatedUser);
      return mapBabaUser(user);
    }
  }

  private static BabaUser mapBabaUser(User user) {
    BabaUser babaUser = new BabaUser();
    babaUser.username = user.getUsername();
    babaUser.contactDetails = new BabaContactDetails();
    babaUser.contactDetails.firstName = user.getContactDetails().getFirstName();
    babaUser.contactDetails.lastName = user.getContactDetails().getLastName();
    babaUser.contactDetails.email = user.getContactDetails().getEmail();
    return babaUser;
  }

  /**
   * Return the role assignments for an OAuth2 subject.
   * Role assignments for actual users are extracted from the Baba database.
   * Role assignments for Entur Partner machine-to-machine tokens are built from configuration.
   * Role assignments for Entur Internal machine-to-machine tokens are mapped directly from the permissions listed in the token.
   */
  public List<RoleAssignment> roleAssignments(AuthenticatedUser authenticatedUser) {
    if (authenticatedUser.isClient()) {
      if (authenticatedUser.isInternal() || authenticatedUser.isPartner()) {
        return enturClientM2MRoleAssignmentRepository.getRolesAssignments(authenticatedUser);
      } else {
        throw new IllegalArgumentException("Unknown client " + authenticatedUser);
      }
    } else if (authenticatedUser.isRor()) {
      User user = repository.getUserByUsername(authenticatedUser.username());
      if (user == null) {
        throw new NotFoundException(
          "User with user name: [" + authenticatedUser.subject() + "] not found"
        );
      }
      return toRoleAssignments(user);
    } else {
      User user = permissionStoreUser(authenticatedUser);
      return toRoleAssignments(user);
    }
  }

  private User permissionStoreUser(AuthenticatedUser authenticatedUser) {
    LOGGER.debug("Retrieving user {} in Entur Partner", authenticatedUser.subject());
    PermissionStoreUser permissionStoreUser = permissionStoreClient.getUser(
      authenticatedUser.subject()
    );
    if (permissionStoreUser == null) {
      LOGGER.debug("User not found in Entur Partner: {}", authenticatedUser.subject());
      throw new NotFoundException(
        "User with subject '" + authenticatedUser.subject() + "' not found in Entur Partner"
      );
    }
    LOGGER.debug(
      "Found Entur Partner user for subject {} : {}",
      authenticatedUser.subject(),
      permissionStoreUser
    );
    if (permissionStoreUser.email == null) {
      LOGGER.debug("User without email in Entur Partner: {}", authenticatedUser.subject());
      throw new ServerErrorException(
        "User with subject '" + authenticatedUser.subject() + "' has no email in Entur Partner",
        Response.Status.INTERNAL_SERVER_ERROR
      );
    }
    String normalizedEmail = permissionStoreUser.email.toLowerCase();
    LOGGER.debug("Retrieving user with email '{}' in Baba database", normalizedEmail);
    User user = repository.getUserByEmail(normalizedEmail);
    if (user == null) {
      LOGGER.debug(
        "No user found in Baba database with email '{}' : permission store user = {}",
        normalizedEmail,
        permissionStoreUser
      );
      throw new NotFoundException("User with subject: [" + authenticatedUser + "] not found");
    }
    LOGGER.debug(
      "Found user with email '{}' in Baba database: '{}'",
      normalizedEmail,
      user.getUsername()
    );
    return user;
  }

  private List<RoleAssignment> toRoleAssignments(User user) {
    List<RoleAssignment> list = user
      .getResponsibilitySets()
      .stream()
      .map(ResponsibilitySet::getRoles)
      .flatMap(Collection::stream)
      .map(RoleAssignmentMapper::toRoleAssignment)
      .toList();
    return list;
  }

  public String listUsers() {
    Map<String, Long> organisationIdByCodespace = organisationIdByCodespace();
    return repository
      .findAll()
      .stream()
      .filter(User::isPersonalAccount)
      .sorted(Comparator.comparing(User::getUsername))
      .map(user -> mapUser(user, organisationIdByCodespace))
      .collect(Collectors.joining("\n"));
  }

  public String jsonlistUsers() {
    Map<String, Long> organisationIdByCodespace = organisationIdByCodespace();
    String userExport = toJson(
      repository
        .findAll()
        .stream()
        .filter(User::isPersonalAccount)
        .filter(this::shouldExport)
        .sorted(Comparator.comparing(User::getUsername))
        .map(user -> mapUserJson(user, organisationIdByCodespace))
        .toList()
    );
    System.out.println(userExport);
    return userExport;
  }

  private Map<String, Long> organisationIdByCodespace() {
    List<CodespaceMapping> codespaceMappings = organisationRegisterClient.getCodespaceMappings();
    Map<String, Long> organisationIdByCodespace = codespaceMappings
      .stream()
      .collect(
        Collectors.toMap(
          codespaceMapping -> codespaceMapping.codespaces().getFirst(),
          CodespaceMapping::organisationId,
          (id1, id2) -> id1
        )
      );
    organisationIdByCodespace.put("RB", 1L);
    organisationIdByCodespace.put("ENT", 282L);
    organisationIdByCodespace.put("VYG", 107L);
    organisationIdByCodespace.put("VKT", 32L);
    organisationIdByCodespace.put("SOF", 24L);
    return organisationIdByCodespace;
  }

  private boolean shouldExport(User user) {
    // prevent rate limiting
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    String email = user.getContactDetails().getEmail();
    boolean isFederated = permissionStoreClient.isFederated(email);
    boolean existsInEnturPartner = isFederated || iamService.hasUserWithEmail(email);
    return !existsInEnturPartner;
  }

  private String toJson(Object object) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, object);
      return writer.toString();
    } catch (IOException e) {
      throw new BabaException(e);
    }
  }

  private com.auth0.json.mgmt.users.User mapUserJson(
    User user,
    Map<String, Long> organisationIdByCodespace
  ) {
    com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
    auth0User.setEmail(user.getContactDetails().getEmail());
    auth0User.setGivenName(user.getContactDetails().getFirstName());
    auth0User.setFamilyName(user.getContactDetails().getLastName());
    auth0User.setNickname(user.getUsername());
    auth0User.setAppMetadata(
      Map.of(
        "organisationID",
        organisationIdByCodespace.get(user.getOrganisation().getPrivateCode()),
        "employeeID",
        ""
      )
    );
    return auth0User;
  }

  private String mapUser(User user, Map<String, Long> codespaceByOrganisationId) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    String email = user.getContactDetails().getEmail();
    boolean isFederated = permissionStoreClient.isFederated(email);
    boolean existsInEnturPartner = isFederated || iamService.hasUserWithEmail(email);
    String userInfo =
      user.getUsername() +
      "," +
      email +
      "," +
      user.getOrganisation().getPrivateCode() +
      "," +
      isFederated +
      "," +
      existsInEnturPartner +
      "," +
      codespaceByOrganisationId.get(user.getOrganisation().getPrivateCode());
    System.out.println(userInfo);
    return userInfo;
  }

  public String notifyUsers() {
    return "";
    /*return repository.findAll().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .filter(User::isPersonalAccount)
                .filter(this::shouldExport)
                .map(this::sendEmail).
                collect(Collectors.joining("\n"));*/

  }

  public String notifySSOUsers() {
    //return "";
    return repository
      .findAll()
      .stream()
      .sorted(Comparator.comparing(User::getUsername))
      .filter(User::isPersonalAccount)
      .filter(this::isFederatedUser)
      .map(this::sendEmail)
      .collect(Collectors.joining("\n"));
  }

  public String notifyNonSSOUsers() {
    //return "";
    return repository
      .findAll()
      .stream()
      .sorted(Comparator.comparing(User::getUsername))
      .filter(User::isPersonalAccount)
      .filter(user -> !isFederatedUser(user))
      .map(this::sendEmail)
      .collect(Collectors.joining("\n"));
  }

  public void updatePrivateCode() {
    repository
      .findAll()
      .stream()
      .sorted(Comparator.comparing(User::getUsername))
      .filter(User::isPersonalAccount)
      .forEach(this::updatePrivateCodeForUser);
  }

  private void updatePrivateCodeForUser(User user) {
    String subject = iamService.getUserWithEmail(user.getContactDetails().getEmail());
    user.setPrivateCode(subject);
  }

  private boolean isFederatedUser(User user) {
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return permissionStoreClient.isFederated(user.getContactDetails().getEmail());
  }

  private String sendEmail(User user) {
    newUserEmailSender.sendEmail(user);
    return user.getContactDetails().getEmail();
  }
}
