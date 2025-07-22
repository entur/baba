package no.rutebanken.baba.security.permissionstore;

import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.entur.ror.permission.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static no.rutebanken.baba.security.permissionstore.EnturInternalM2MRoleAssignmentRepository.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnturInternalM2MRoleAssignmentRepositoryTest {

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
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(testRepository);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
                .build();
        assertTrue(repository.getRolesAssignments(authenticatedUser).isEmpty());
    }

    @Test
    void mapRoleAssignmentsFromPermissions() {
        M2MClient client = new M2MClient();
        client.setEnturOrganisationId(TEST_ENTUR_ORGANISATION_ID);
        M2MClientRepository testRepository = new TestM2MClientRepository(client);
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(testRepository);
        List<String> roleAssignments = List.of(
                roleAssignment("a").toString(),
                roleAssignment("b").toString(),
                roleAssignment("c").toString()
        );
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
                .withPermissions(List.of("a", "b", "c"))
                .build();
        assertEquals(roleAssignments, repository.getRolesAssignments(authenticatedUser).stream().map(RoleAssignment::toString).toList());
    }

    @Test
    void mapRoleAssignmentsFromDatabase() {
        M2MClient client = new M2MClient();
        client.setEnturOrganisationId(TEST_ENTUR_ORGANISATION_ID);
        ResponsibilitySet responsibilitySet = new ResponsibilitySet();
        Authority orgAAA = new Authority();
        orgAAA.setPrivateCode(ORG_AAA);
        ResponsibilityRoleAssignment r1 = ResponsibilityRoleAssignment.builder()
                .withCodeSpace(new CodeSpace("RB", null, null))
                .withTypeOfResponsibilityRole(new Role(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW, "view Blocks"))
                .withResponsibleOrganisation(orgAAA)
                .build();
        responsibilitySet.setRoles(Set.of(r1));
        client.setResponsibilitySets(Set.of(responsibilitySet));

        M2MClientRepository testRepository = new TestM2MClientRepository(client);
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(testRepository);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
                .build();

        List<String> expectedRoleAssignments = List.of(
                RoleAssignment.builder().withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW).withOrganisation(ORG_AAA).build().toString()
        );

        assertEquals(expectedRoleAssignments, repository.getRolesAssignments(authenticatedUser).stream().map(RoleAssignment::toString).toList());
    }


    @Test
    void testClientNameInDatabase() {
        M2MClient client = new M2MClient();
        client.setName(TEST_CLIENT_NAME);
        M2MClientRepository testRepository = new TestM2MClientRepository(client);
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(testRepository);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .withOrganisationId(TEST_ENTUR_ORGANISATION_ID)
                .build();
        String clientName = repository.getClientName(authenticatedUser);
        assertEquals(TEST_CLIENT_NAME, clientName);
    }

    @Test
    void testClientNameNotInDatabase() {
        M2MClientRepository testRepository = new TestM2MClientRepository(null);
        EnturInternalM2MRoleAssignmentRepository repository = new EnturInternalM2MRoleAssignmentRepository(testRepository);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser.AuthenticatedUserBuilder()
                .withSubject(TEST_CLIENT_SUBJECT)
                .withIssuer(TEST_ISSUER)
                .build();
        String clientName = repository.getClientName(authenticatedUser);
        assertEquals("Entur Internal/" + clientId(TEST_CLIENT_SUBJECT), clientName);
    }


    private static RoleAssignment roleAssignment(String a) {
        return RoleAssignment.builder().withOrganisation(DEFAULT_ADMIN_ORG).withRole(a).build();
    }

    private static class TestM2MClientRepository implements M2MClientRepository {
        private final M2MClient client;

        public TestM2MClientRepository(M2MClient client) {
            this.client = client;
        }

        @Override
        public M2MClient getOneByPublicId(String id) {
            throw new NotImplementedException();
        }

        @Override
        public M2MClient getOneByPublicIdIfExists(String id) {
            return client;
        }

        @Override
        public List<M2MClient> findAll() {
            throw new NotImplementedException();
        }

        @Override
        public void flush() {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> S saveAndFlush(S entity) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> List<S> saveAllAndFlush(Iterable<S> entities) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAllInBatch(Iterable<M2MClient> entities) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAllInBatch() {
            throw new NotImplementedException();
        }

        @Override
        public M2MClient getOne(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public M2MClient getById(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public M2MClient getReferenceById(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> List<S> findAll(Example<S> example) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> List<S> findAll(Example<S> example, Sort sort) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> List<S> saveAll(Iterable<S> entities) {
            throw new NotImplementedException();
        }

        @Override
        public List<M2MClient> findAllById(Iterable<Long> longs) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> S save(S entity) {
            throw new NotImplementedException();
        }

        @Override
        public Optional<M2MClient> findById(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public boolean existsById(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public long count() {
            throw new NotImplementedException();
        }

        @Override
        public void deleteById(Long aLong) {
            throw new NotImplementedException();
        }

        @Override
        public void delete(M2MClient entity) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAll(Iterable<? extends M2MClient> entities) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAll() {
            throw new NotImplementedException();
        }

        @Override
        public List<M2MClient> findAll(Sort sort) {
            throw new NotImplementedException();
        }

        @Override
        public Page<M2MClient> findAll(Pageable pageable) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> Optional<S> findOne(Example<S> example) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> Page<S> findAll(Example<S> example, Pageable pageable) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> long count(Example<S> example) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient> boolean exists(Example<S> example) {
            throw new NotImplementedException();
        }

        @Override
        public <S extends M2MClient, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            throw new NotImplementedException();
        }
    }
}