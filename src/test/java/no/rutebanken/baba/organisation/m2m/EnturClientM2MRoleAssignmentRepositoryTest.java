package no.rutebanken.baba.organisation.m2m;

import static no.rutebanken.baba.organisation.m2m.EnturClientM2MRoleAssignmentRepository.CLIENT_SUBJECT_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.entur.ror.permission.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;

class EnturClientM2MRoleAssignmentRepositoryTest {

  private static final String TEST_CLIENT_ID = "testClientId";
  private static final String TEST_CLIENT_NAME = "testClientName";
  private static final String TEST_ISSUER = "testIssuer";
  private static final long TEST_ENTUR_ORGANISATION_ID = 1L;
  private static final String TEST_CLIENT_SUBJECT = TEST_CLIENT_ID + CLIENT_SUBJECT_SUFFIX;
  private static final String ORG_AAA = "AAA";

  @Test
  void mapEmptyRoleAssignments() {
    M2MClient client = new M2MClient();
    client.setEnturOrganisationId(TEST_ENTUR_ORGANISATION_ID);
    M2MClientRepository testRepository = new TestM2MClientRepository(client);
    EnturClientM2MRoleAssignmentRepository repository = new EnturClientM2MRoleAssignmentRepository(
      testRepository
    );
    AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
      .withSubject(TEST_CLIENT_SUBJECT)
      .withIssuer(TEST_ISSUER)
      .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
      .build();
    assertTrue(repository.getRolesAssignments(authenticatedUser).isEmpty());
  }

  @Test
  void mapRoleAssignmentsFromDatabase() {
    M2MClient client = new M2MClient();
    client.setEnturOrganisationId(TEST_ENTUR_ORGANISATION_ID);
    ResponsibilitySet responsibilitySet = new ResponsibilitySet();
    Authority orgAAA = new Authority();
    orgAAA.setPrivateCode(ORG_AAA);
    ResponsibilityRoleAssignment r1 = ResponsibilityRoleAssignment
      .builder()
      .withCodeSpace(new CodeSpace("RB", null, null))
      .withTypeOfResponsibilityRole(
        new Role(AuthorizationConstants.ROLE_NETEX_PRIVATE_DATA_VIEW, "view Blocks")
      )
      .withResponsibleOrganisation(orgAAA)
      .build();
    responsibilitySet.setRoles(Set.of(r1));
    client.setResponsibilitySets(Set.of(responsibilitySet));

    M2MClientRepository testRepository = new TestM2MClientRepository(client);
    EnturClientM2MRoleAssignmentRepository repository = new EnturClientM2MRoleAssignmentRepository(
      testRepository
    );
    AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
      .withSubject(TEST_CLIENT_SUBJECT)
      .withIssuer(TEST_ISSUER)
      .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
      .build();

    List<String> expectedRoleAssignments = List.of(
      RoleAssignment
        .builder()
        .withRole(AuthorizationConstants.ROLE_NETEX_PRIVATE_DATA_VIEW)
        .withOrganisation(ORG_AAA)
        .build()
        .toString()
    );

    assertEquals(
      expectedRoleAssignments,
      repository
        .getRolesAssignments(authenticatedUser)
        .stream()
        .map(RoleAssignment::toString)
        .toList()
    );
  }

  @Test
  void testClientNameInDatabase() {
    M2MClient client = new M2MClient();
    client.setName(TEST_CLIENT_NAME);
    M2MClientRepository testRepository = new TestM2MClientRepository(client);
    EnturClientM2MRoleAssignmentRepository repository = new EnturClientM2MRoleAssignmentRepository(
      testRepository
    );
    AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
      .withSubject(TEST_CLIENT_SUBJECT)
      .withIssuer(TEST_ISSUER)
      .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
      .build();
    String clientName = repository.getClientName(authenticatedUser);
    assertEquals(TEST_CLIENT_NAME, clientName);
  }
}
