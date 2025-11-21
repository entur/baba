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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.rest.dto.BaseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

@Tags(value = { @Tag(name = "AnnotatedBaseResource", description = "Annotated Base Resource") })
public abstract class AnnotatedBaseResource<E extends VersionedEntity, D extends BaseDTO>
  extends BaseResource<E, D> {

  @POST
  @PreAuthorize("@authorizationService.isOrganisationAdmin()")
  public Response create(D dto, @Context UriInfo uriInfo) {
    return super.createEntity(dto, uriInfo);
  }

  @PUT
  @Path("{id}")
  @PreAuthorize("@authorizationService.isOrganisationAdmin()")
  public void update(@PathParam("id") String id, D dto) {
    super.updateEntity(id, dto);
  }

  @GET
  @Path("{id}")
  @PreAuthorize("@authorizationService.isOrganisationAdmin()")
  public D get(@PathParam("id") String id) {
    return super.getEntity(id);
  }

  @DELETE
  @Path("{id}")
  @PreAuthorize("@authorizationService.isOrganisationAdmin()")
  public void delete(@PathParam("id") String id) {
    super.deleteEntity(id);
  }

  @GET
  @PreAuthorize("@authorizationService.isOrganisationAdmin()")
  public List<D> listAll() {
    return super.listAllEntities();
  }
}
