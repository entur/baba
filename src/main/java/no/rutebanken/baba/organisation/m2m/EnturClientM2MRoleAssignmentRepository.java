package no.rutebanken.baba.organisation.m2m;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build role assignments for Entur Client machine-to-machine tokens.
 * Role assignments are built from the Baba database.
 * TODO Permission Store migration
 */
public class EnturClientM2MRoleAssignmentRepository {

  static final String CLIENT_SUBJECT_SUFFIX = "@clients";

  private static final Logger LOGGER = LoggerFactory.getLogger(
    EnturClientM2MRoleAssignmentRepository.class
  );

  private final M2MClientRepository repository;

  public EnturClientM2MRoleAssignmentRepository(M2MClientRepository repository) {
    this.repository = repository;
  }

  /**
   * Extract RoleAssignments from the baba database.
   */
  public List<RoleAssignment> getRolesAssignments(AuthenticatedUser authenticatedUser) {
    validateM2MClient(authenticatedUser);
    List<RoleAssignment> rolesFromDatabase = getRoleAssignmentsFromDatabase(
      authenticatedUser,
      repository
    );
    LOGGER.info(
      "Returning {} role assignments from database for client {} and organisation {}",
      rolesFromDatabase.size(),
      authenticatedUser.subject(),
      authenticatedUser.organisationId()
    );
    return rolesFromDatabase;
  }

  public String getClientName(AuthenticatedUser authenticatedUser) {
    String clientId = clientId(authenticatedUser.subject());
    M2MClient client = repository.getOneByPublicId(clientId);
    return client.getName();
  }

  /**
   * Extract the OAuth2 client-id from the subject claim.
   * The subject claim structure for m2m clients is "client-id@clients"
   */
  private static String clientId(String subject) {
    return subject.replace(CLIENT_SUBJECT_SUFFIX, "");
  }

  /**
   * Extract the role assignments from a given M2M client.
   */
  private static Stream<RoleAssignment> toRoleAssignments(M2MClient client) {
    return client
      .getResponsibilitySets()
      .stream()
      .map(ResponsibilitySet::getRoles)
      .flatMap(Collection::stream)
      .map(RoleAssignmentMapper::toRoleAssignment);
  }

  private static void validateM2MClient(AuthenticatedUser authenticatedUser) {
    if (!authenticatedUser.isClient()) {
      throw new IllegalArgumentException(
        "The user is not a machine-to-machine client: " + authenticatedUser.subject()
      );
    }
  }

  private static List<RoleAssignment> getRoleAssignmentsFromDatabase(
    AuthenticatedUser authenticatedUser,
    M2MClientRepository repository
  ) {
    M2MClient client = repository.getOneByPublicIdIfExists(clientId(authenticatedUser.subject()));
    if (client == null) {
      return List.of();
    }
    if (authenticatedUser.organisationId() != client.getEnturOrganisationId()) {
      throw new IllegalArgumentException(
        "Organisation id mismatch: expected " +
        client.getEnturOrganisationId() +
        ", but was " +
        authenticatedUser.organisationId()
      );
    }
    if (
      (authenticatedUser.isInternal() && !client.isInternal()) ||
      (authenticatedUser.isPartner() && !client.isPartner())
    ) {
      throw new IllegalArgumentException("Issuer mismatch: expected " + client.getIssuer());
    }
    return toRoleAssignments(client).toList();
  }
}
