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

package no.rutebanken.baba.organisation.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import no.rutebanken.baba.organisation.email.NewUserEmailSender;
import no.rutebanken.baba.organisation.model.OrganisationException;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.RoleRepository;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.mapper.UserMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import no.rutebanken.baba.organisation.rest.validation.UserValidator;
import no.rutebanken.baba.organisation.service.IamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ORGANISATION_EDIT;

@Component
@Path("users")
@Produces("application/json")
@Transactional
@PreAuthorize("hasRole('" + ROLE_ORGANISATION_EDIT + "')")
@Tags(value = {
        @Tag(name = "UserResource", description = "User resource")
})
public class UserResource extends BaseResource<User, UserDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    private final UserRepository repository;

    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final UserValidator validator;
    private final IamService iamService;

    private final NewUserEmailSender newUserEmailSender;

    public UserResource(UserRepository repository, RoleRepository roleRepository,
                        UserMapper mapper,
                        UserValidator validator,
                        IamService iamService,
                        NewUserEmailSender newUserEmailSender) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.validator = validator;
        this.iamService = iamService;
        this.newUserEmailSender = newUserEmailSender;
    }

    @GET
    @Path("{id}")
    public UserDTO get(@PathParam("id") String id, @QueryParam("full") boolean fullObject) {
        User entity = getExisting(id);
        return getMapper().toDTO(entity, fullObject);
    }

    /**
     * Create a new user.
     * If the user account is not a personal account, it is created only in the local database (baba) and not inserted in the Auth0 tenant.
     * Do not wrap method in a single transaction. Need to commit local storage before creating user in IAM, to avoid having users in IAM that does not exist in local storage.
     */
    @Transactional(propagation = Propagation.NEVER)
    @POST
    public Response create(UserDTO dto, @Context UriInfo uriInfo) {
        User user = createEntity(dto);
        if (user.isPersonalAccount()) {
            boolean created;
            try {
                created = iamService.createOrUpdate(user);
            } catch (RuntimeException e) {
                LOGGER.warn("Creation of new user in IAM failed. Removing user from local storage. Exception: {}", e.getMessage(), e);
                deleteEntity(user.getId());
                throw new OrganisationException("Creation of new user in IAM failed", e);
            }
            if (created) {
                newUserEmailSender.sendEmail(user);
            }
        }
        return buildCreatedResponse(uriInfo, user);
    }


    @PUT
    @Path("{id}")
    public void update(@PathParam("id") String id, UserDTO dto) {
        User user = updateEntity(id, dto);
        if (user.isPersonalAccount()) {
            iamService.createOrUpdate(user);
        }
    }

    // TODO temporary service to migrate accounts to Auth0
// to be removed after the migration is complete
    @POST
    @Path("migrate")
    public void migrate() throws InterruptedException {
        LOGGER.info("Migrating user accounts to Auth0");
        for (UserDTO userDTO : listAllEntities()) {
            User user = getExisting(userDTO.id);

            // Notification accounts do not need to be migrated
            if (!user.isPersonalAccount()) {
                continue;
            }
            LOGGER.info("Migrating user {}", user.getUsername());
            boolean created = iamService.createOrUpdate(user);
            if (created) {
                LOGGER.info("Sending email to user {}", user.getUsername());
                newUserEmailSender.sendEmail(user);
            } else {
                LOGGER.info("The user {} was already migrated", user.getUsername());
            }
            // slow down migration to prevent rate limiting
            Thread.sleep(10000);
        }
        LOGGER.info("Migration to Auth0 complete");
    }


    // TODO temporary service to migrate accounts to Auth0
// to be removed after the migration is complete
    @POST
    @Path("migrate_roles")
    public void migrateRoles() throws InterruptedException {
        LOGGER.info("Migrating roles to Auth0");
        for (Role role : roleRepository.findAll()) {
            LOGGER.info("Migrating role {}", role.getName());
            // TODO skip when role exists already
            iamService.createRole(role);
            // slow down migration to prevent rate limiting
            Thread.sleep(10000);
        }
        //TODO retrieve default role
        iamService.createRole(new Role("rutebanken", "Default Role"));
        LOGGER.info("Migration of roles to Auth0 complete");
    }


    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") String id) {
        User user = deleteEntity(id);
        if (user.isPersonalAccount()) {
            iamService.removeUser(user);
        }
    }

    @GET
    public List<UserDTO> listAll(@QueryParam("full") boolean fullObject) {
        return super.listAllEntities(fullObject);
    }

    @Override
    protected VersionedEntityRepository<User> getRepository() {
        return repository;
    }

    @Override
    protected DTOMapper<User, UserDTO> getMapper() {
        return mapper;
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected DTOValidator<User, UserDTO> getValidator() {
        return validator;
    }

}
