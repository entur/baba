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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.Set;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import no.rutebanken.baba.organisation.rest.mapper.NotificationConfigurationMapper;
import no.rutebanken.baba.organisation.rest.validation.NotificationConfigurationValidator;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("users/{userName}/notification_configurations")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(
  value = {
    @Tag(
      name = "NotificationConfigurationResource",
      description = "Notification configuration resource"
    ),
  }
)
public class NotificationConfigurationResource {

  private final UserRepository repository;
  private final NotificationConfigurationMapper mapper;
  private final NotificationConfigurationValidator validator;

  public NotificationConfigurationResource(
    UserRepository repository,
    NotificationConfigurationMapper mapper,
    NotificationConfigurationValidator validator
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validator = validator;
  }

  @GET
  @PreAuthorize("#userName == authentication.name or @authorizationService.isOrganisationAdmin()")
  public Set<NotificationConfigDTO> get(
    @PathParam("userName") String userName,
    @QueryParam("full") boolean fullObject
  ) {
    User entity = getUser(userName);
    return mapper.toDTO(entity.getNotificationConfigurations(), false);
  }

  @PUT
  @PreAuthorize("#userName == authentication.name or @authorizationService.isOrganisationAdmin()")
  public void createOrUpdate(
    @PathParam("userName") String userName,
    Set<NotificationConfigDTO> config
  ) {
    validator.validate(userName, config);
    User user = getUser(userName.toLowerCase());
    user.setNotificationConfigurations(mapper.fromDTO(config));
    repository.save(user);
  }

  @DELETE
  @PreAuthorize("#userName == authentication.name or @authorizationService.isOrganisationAdmin()")
  public void delete(@PathParam("userName") String userName) {
    User user = getUser(userName);
    user.getNotificationConfigurations().clear();
    repository.save(user);
  }

  protected User getUser(String userName) {
    User user;
    try {
      user = repository.getUserByUsername(userName);
    } catch (DataRetrievalFailureException e) {
      user = null;
    }

    if (user == null) {
      throw new NotFoundException("User with user name: [" + userName + "] not found");
    }
    return user;
  }
}
