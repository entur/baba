package no.rutebanken.baba.organisation.service;

import static no.rutebanken.baba.organisation.service.IamUtils.generatePassword;
import static no.rutebanken.baba.organisation.util.RoleAssignmentMapper.toAtr;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.OrganisationException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.RoleRepository;
import no.rutebanken.baba.organisation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the IamService that updates a remote Auth0 tenant.
 */
public class Auth0IamService implements IamService {

  private static final String AUTH0_CONNECTION = "Username-Password-Authentication";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final List<String> defaultRoles;

  private final String domain;

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final AuthAPI authAPI;

  private TokenHolder tokenHolder;
  private Instant accessTokenRetrievedAt;
  private ManagementAPI managementAPI;
  private final Clock clock = Clock.systemUTC();

  public Auth0IamService(
    UserRepository userRepository,
    RoleRepository roleRepository,
    AuthAPI authAPI,
    List<String> defaultRoles,
    String domain
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.authAPI = authAPI;
    this.defaultRoles = defaultRoles;
    this.domain = domain;
  }

  @Override
  public void createUser(User user) {
    logger.info("Creating user {} in Auth0", user.getUsername());
    String password = generatePassword();
    com.auth0.json.mgmt.users.User auth0User = toAuth0User(user);
    auth0User.setPassword(password.toCharArray());
    com.auth0.json.mgmt.users.User createdUser = null;
    try {
      createdUser = getManagementAPI().users().create(auth0User).execute().getBody();
      logger.info("Created user {} with Auth0 id {}", user.getUsername(), createdUser.getId());
      updateRoles(user, createdUser, roleRepository.findAll());
    } catch (Exception e) {
      logger.error("User creation failed for user {}", user, e);
      if (createdUser != null) {
        logger.info("Attempting to remove user {}", user);
        removeUser(user);
      }
      throw new OrganisationException("User creation failed");
    }
  }

  @Override
  public void updateUser(User user) {
    logger.info("Updating user {} in Auth0", user.getUsername());
    com.auth0.json.mgmt.users.User existingAuth0User = getAuth0UserByUsername(user.getUsername());
    com.auth0.json.mgmt.users.User updatedAuth0User = toAuth0User(user);
    // The Auth0 API refuses to update both the username and the email at the same time
    updatedAuth0User.setUsername(null);
    try {
      getManagementAPI().users().update(existingAuth0User.getId(), updatedAuth0User).execute();
      updateRoles(user, existingAuth0User, roleRepository.findAll());
      logger.info("User {} successfully updated in Auth0", user.getUsername());
    } catch (Exception e) {
      logger.error("User update in Auth0 failed for user {}", user.getUsername(), e);
      throw new OrganisationException("User update in Auth0 failed");
    }
  }

  @Override
  public void removeUser(User user) {
    logger.info("Removing user {} from Auth0", user.getUsername());
    com.auth0.json.mgmt.users.User existingAuth0User;
    try {
      existingAuth0User = getAuth0UserByUsername(user.getUsername());
    } catch (OAuth2UserNotFoundException nfe) {
      logger.warn(
        "Ignoring user removal for user {} that does not exist in the Auth0 tenant",
        user.getUsername()
      );
      return;
    }
    try {
      getManagementAPI().users().delete(existingAuth0User.getId()).execute();
      logger.info("User {} successfully removed from Auth0", user.getUsername());
    } catch (Auth0Exception e) {
      logger.error("Failed to remove user {} from Auth0", user.getUsername(), e);
      throw new OrganisationException("Failed to remove user from Auth0");
    }
  }

  @Override
  public void createRole(Role role) {
    logger.info("Creating role {} in Auth0: ", role.getId());
    com.auth0.json.mgmt.roles.Role auth0Role = toAuth0Role(role);
    try {
      com.auth0.json.mgmt.roles.Role createdRole = getManagementAPI()
        .roles()
        .create(auth0Role)
        .execute()
        .getBody();
      logger.info("Role successfully created in Auth0: {}", createdRole.getId());
    } catch (Exception e) {
      logger.error("Failed to create role {}", role.getId(), e);
      throw new OrganisationException("Failed to create role");
    }
  }

  @Override
  public void removeRole(Role role) {
    logger.info("Removing role in Auth0: {}", role.getId());
    com.auth0.json.mgmt.roles.Role auth0Role;
    try {
      auth0Role = getAuth0RoleByPrivateCode(role.getPrivateCode());
    } catch (OAuth2RoleNotFoundException e) {
      logger.warn(
        "Ignoring removal of role {} that does not exist in the Auth0 tenant",
        role.getPrivateCode()
      );
      return;
    }
    try {
      getManagementAPI().roles().delete(auth0Role.getId()).execute();
      logger.info("Role {} successfully removed from Auth0", role.getId());
    } catch (Exception e) {
      logger.error("Failed to remove role {} from Auth0", role.getId(), e);
      throw new OrganisationException("Failed to remove role from Auth0");
    }
  }

  @Override
  public void updateResponsibilitySet(ResponsibilitySet responsibilitySet) {
    logger.info("Updating responsibility set {} in Auth0", responsibilitySet.getId());
    List<Role> systemRoles = roleRepository.findAll();
    try {
      userRepository
        .findUsersWithResponsibilitySet(responsibilitySet)
        .forEach(u -> {
          com.auth0.json.mgmt.users.User auth0User = getAuth0UserByUsername(u.getUsername());
          updateRoles(u, auth0User, systemRoles);
        });
    } catch (Exception e) {
      logger.info("Failed to update responsibility set {} in Auth0", responsibilitySet.getId(), e);
      throw new OrganisationException("Failed to update responsibility set in Auth0");
    }
  }

  private synchronized ManagementAPI getManagementAPI() throws Auth0Exception {
    if (managementAPI == null) {
      refreshToken();

      Auth0HttpClient auth0HttpClient = DefaultHttpClient
        .newBuilder()
        .withConnectTimeout(10)
        .withReadTimeout(10)
        .withMaxRetries(10)
        .build();

      managementAPI =
        ManagementAPI
          .newBuilder(domain, tokenHolder.getAccessToken())
          .withHttpClient(auth0HttpClient)
          .build();
    }
    if (hasTokenExpired()) {
      refreshToken();
      managementAPI.setApiToken(tokenHolder.getAccessToken());
    }
    return managementAPI;
  }

  /**
   * The token is considered expired 60 seconds before its actual expiration date.
   *
   * @return true if the token is about to expire.
   */
  private boolean hasTokenExpired() {
    return clock
      .instant()
      .isAfter(accessTokenRetrievedAt.plus(tokenHolder.getExpiresIn() - 60, ChronoUnit.SECONDS));
  }

  private void refreshToken() throws Auth0Exception {
    logger.debug("Refreshing Admin API token");
    TokenRequest tokenRequest = authAPI.requestToken("https://" + domain + "/api/v2/");
    tokenHolder = tokenRequest.execute().getBody();
    accessTokenRetrievedAt = clock.instant();
  }

  /**
   * Update the user roles.
   * Only roles that are defined in the organisation repository are added or removed.
   * Roles that are assigned to the user in Auth0 but that are not defined in the organisation repository are ignored.
   *
   * @param user        the user in the organisation repository whose roles should be updated in Auth0.
   * @param auth0User   the user in Auth0.
   * @param systemRoles all the roles that are defined in the organisation repository.
   */
  private void updateRoles(
    User user,
    com.auth0.json.mgmt.users.User auth0User,
    List<Role> systemRoles
  ) {
    logger.info("Updating roles for user {}", user.getUsername());
    try {
      // all the role names defined in the organisation repository
      Set<String> systemRoleNames = systemRoles
        .stream()
        .map(Role::getId)
        .collect(Collectors.toSet());
      // the role names assigned to the user in the organisation repository
      Set<String> newRoleNames = getRoleNames(user);
      // the Auth0 roles currently assigned to the Auth0 user
      List<com.auth0.json.mgmt.roles.Role> existingAuth0UserRoles = getManagementAPI()
        .users()
        .listRoles(auth0User.getId(), null)
        .execute()
        .getBody()
        .getItems();
      // Auth0 role ids that are currently assigned to the Auth0 user and that should be removed
      List<String> auth0RoleIdsToBeRemoved = existingAuth0UserRoles
        .stream()
        .filter(r -> systemRoleNames.contains(r.getName()))
        .filter(r -> !newRoleNames.remove(r.getName()))
        .map(com.auth0.json.mgmt.roles.Role::getId)
        .toList();

      if (!auth0RoleIdsToBeRemoved.isEmpty()) {
        getManagementAPI()
          .users()
          .removeRoles(auth0User.getId(), auth0RoleIdsToBeRemoved)
          .execute();
      }

      if (!newRoleNames.isEmpty()) {
        List<String> rolesToBeAdded = newRoleNames
          .stream()
          .map(this::getAuth0RoleByPrivateCode)
          .map(com.auth0.json.mgmt.roles.Role::getId)
          .toList();
        getManagementAPI().users().addRoles(auth0User.getId(), rolesToBeAdded).execute();
      }
    } catch (Auth0Exception e) {
      logger.error("Failed to update roles for user {}", user.getUsername(), e);
      throw new OrganisationException("Failed to update roles");
    }
  }

  private com.auth0.json.mgmt.users.User toAuth0User(User user) {
    com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User(AUTH0_CONNECTION);
    auth0User.setUsername(user.getUsername());
    auth0User.setGivenName(user.getContactDetails().getFirstName());
    auth0User.setFamilyName(user.getContactDetails().getLastName());
    auth0User.setEmail(user.getContactDetails().getEmail());
    auth0User.setName(auth0User.getGivenName() + ' ' + auth0User.getFamilyName());

    if (user.getResponsibilitySets() != null) {
      Map<String, Object> attributes = new HashMap<>();
      List<String> roleAssignments = new ArrayList<>();

      for (ResponsibilitySet responsibilitySet : user.getResponsibilitySets()) {
        if (responsibilitySet.getRoles() != null) {
          responsibilitySet.getRoles().forEach(rra -> roleAssignments.add(toAtr(rra)));
        }
      }

      attributes.put("roles", roleAssignments);
      auth0User.setAppMetadata(attributes);
    }

    return auth0User;
  }

  private com.auth0.json.mgmt.roles.Role toAuth0Role(Role role) {
    com.auth0.json.mgmt.roles.Role auth0Role = new com.auth0.json.mgmt.roles.Role();
    auth0Role.setName(role.getId());
    auth0Role.setDescription(role.getName());
    return auth0Role;
  }

  private com.auth0.json.mgmt.users.User getAuth0UserByUsername(String username) {
    try {
      List<com.auth0.json.mgmt.users.User> matchingUsers = getManagementAPI()
        .users()
        .list(new UserFilter().withQuery("username:\"" + username + "\""))
        .execute()
        .getBody()
        .getItems();
      if (matchingUsers.isEmpty()) {
        throw new OAuth2UserNotFoundException("User not found in Auth0: " + username);
      } else if (matchingUsers.size() > 1) {
        logger.error("More than one user found in Auth0 tenant with username: {}", username);
        throw new OrganisationException("More than one user found with username: " + username);
      }
      return matchingUsers.getFirst();
    } catch (Auth0Exception e) {
      logger.error("Failed to retrieve the user {} in Auth0", username, e);
      throw new OrganisationException("Failed to retrieve the user");
    }
  }

  private com.auth0.json.mgmt.roles.Role getAuth0RoleByPrivateCode(String privateCode) {
    try {
      // filtering twice by private code since the Auth0 filter accept all names that contain the filter.
      List<com.auth0.json.mgmt.roles.Role> matchingRoles = getManagementAPI()
        .roles()
        .list(new RolesFilter().withName(privateCode))
        .execute()
        .getBody()
        .getItems()
        .stream()
        .filter(r -> privateCode.equals(r.getName()))
        .toList();
      if (matchingRoles.isEmpty()) {
        logger.warn("Role not found: {}", privateCode);
        throw new OAuth2RoleNotFoundException("Role not found: " + privateCode);
      } else if (matchingRoles.size() > 1) {
        logger.error("More than one role found with private code: {}", privateCode);
        throw new OrganisationException(
          "More than one role found with private code:" + privateCode
        );
      }
      return matchingRoles.getFirst();
    } catch (Auth0Exception e) {
      logger.error("Failed to retrieve the role {} in Auth0", privateCode, e);
      throw new OrganisationException("Failed to retrieve role in Auth0");
    }
  }

  private Set<String> getRoleNames(User user) {
    Set<String> roleNames = new HashSet<>(defaultRoles);
    for (ResponsibilitySet responsibilitySet : user.getResponsibilitySets()) {
      roleNames.addAll(
        responsibilitySet
          .getRoles()
          .stream()
          .map(r -> r.getTypeOfResponsibilityRole().getId())
          .collect(Collectors.toSet())
      );
    }
    return roleNames;
  }
}
