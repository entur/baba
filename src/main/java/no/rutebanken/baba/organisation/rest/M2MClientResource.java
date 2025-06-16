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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.mapper.M2MClientMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import no.rutebanken.baba.organisation.rest.validation.M2MClientValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("m2m_clients")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(value = { @Tag(name = "M2MClientResource", description = "M2M Client Resource") })
public class M2MClientResource extends BaseResource<M2MClient, M2MClientDTO> {

  private final VersionedEntityRepository<M2MClient> repository;
  private final DTOMapper<M2MClient, M2MClientDTO> mapper;
  private final DTOValidator<M2MClient, M2MClientDTO> validator;

  public M2MClientResource(
    M2MClientRepository repository,
    M2MClientMapper mapper,
    M2MClientValidator validator
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validator = validator;
  }

  @GET
  @Path("{id}")
  public M2MClientDTO get(@PathParam("id") String id, @QueryParam("full") boolean fullObject) {
    M2MClient entity = getExisting(id);
    return getMapper().toDTO(entity, fullObject);
  }

  @POST
  public Response create(M2MClientDTO dto, @Context UriInfo uriInfo) {
    M2MClient client = createEntity(dto);
    return buildCreatedResponse(uriInfo, client);
  }

  @PUT
  @Path("{id}")
  public void update(@PathParam("id") String id, M2MClientDTO dto) {
    updateEntity(id, dto);
  }

  @DELETE
  @Path("{id}")
  public void delete(@PathParam("id") String id) {
    deleteEntity(id);
  }

  @GET
  public List<M2MClientDTO> listAll(@QueryParam("full") boolean fullObject) {
    return super.listAllEntities(fullObject);
  }

  @Override
  protected VersionedEntityRepository<M2MClient> getRepository() {
    return repository;
  }

  @Override
  protected DTOMapper<M2MClient, M2MClientDTO> getMapper() {
    return mapper;
  }

  @Override
  protected Class<M2MClient> getEntityClass() {
    return M2MClient.class;
  }

  @Override
  protected DTOValidator<M2MClient, M2MClientDTO> getValidator() {
    return validator;
  }
}
