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
import jakarta.ws.rs.NotFoundException;
import no.rutebanken.baba.organisation.email.NewUserEmailSender;
import no.rutebanken.baba.organisation.model.OrganisationException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.mapper.UserMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import no.rutebanken.baba.organisation.rest.validation.UserValidator;
import no.rutebanken.baba.organisation.service.IamService;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.Collection;
import java.util.List;

@Component
@Path("users")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(value = {
        @Tag(name = "UserResource", description ="User resource")
})
public class UserResource extends BaseResource<User, UserDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    private final UserRepository repository;
    private final UserMapper mapper;
    private final UserValidator validator;
    private final IamService iamService;
    private final NewUserEmailSender newUserEmailSender;

    public UserResource(UserRepository repository,
                        UserMapper mapper,
                        UserValidator validator,
                        IamService iamService,
                        NewUserEmailSender newUserEmailSender) {
        this.repository = repository;
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

    @GET
    @Path("{userName}/roleAssignments")
    public List<RoleAssignment> getRoleAssignments(@PathParam("userName") String userName) {
        User user;
        // A token coming from the Entur Partner Tenant maps the preferred user name to the user email.
        // A token coming from the RoR Partner Tenant maps the preferred user name to the Baba user name.
        // The character "@" is forbidden in Baba user names.
        if (userName.contains("@")) {
            user = repository.getUserByEmail(userName);
        } else {
            user = repository.getUserByUsername(userName);
        }

        if (user == null) {
            throw new NotFoundException("User with user name: [" + userName + "] not found");
        }

        return user.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment)
                .toList();
    }

    /**
     * Do not wrap method in a single transaction. Need to commit local storage before creating user in IAM, to avoid having users in IAM that does not exist in local storage.
     */
    @Transactional(propagation = Propagation.NEVER)
    @POST
    public Response create(UserDTO dto, @Context UriInfo uriInfo) {
        User user = createEntity(dto);
        if(user.isPersonalAccount()) {
            try {
                iamService.createUser(user);
            } catch (RuntimeException e) {
                LOGGER.warn("Creation of new user in IAM failed. Removing user from local storage. Exception: {}", e.getMessage(), e);
                deleteEntity(user.getId());
                throw new OrganisationException("Creation of new user in IAM failed", e);
            }
            newUserEmailSender.sendEmail(user);
        }

        return buildCreatedResponse(uriInfo, user);
    }

    @PUT
    @Path("{id}")
    public void update(@PathParam("id") String id, UserDTO dto) {
        User user = updateEntity(id, dto);
        if(user.isPersonalAccount()) {
            iamService.updateUser(user);
        }
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") String id) {
        User user = deleteEntity(id);
        if(user.isPersonalAccount()) {
            iamService.removeUser(user);
        }
    }


    @POST
    @Path("{id}/resetPassword")
    public void resetPassword(@PathParam("id") String id) {
        User user = getExisting(id);
        if(user.isPersonalAccount()) {
            iamService.resetPassword(user);
            newUserEmailSender.sendEmail(user);
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
