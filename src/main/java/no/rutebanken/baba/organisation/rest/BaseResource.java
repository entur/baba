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

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.BaseDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class BaseResource<E extends VersionedEntity, D extends BaseDTO> {

  protected abstract VersionedEntityRepository<E> getRepository();

  protected abstract DTOMapper<E, D> getMapper();

  protected abstract Class<E> getEntityClass();

  protected abstract DTOValidator<E, D> getValidator();

  public E createEntity(D dto) {
    getValidator().validateCreate(dto);
    E entity = getMapper().createFromDTO(dto, getEntityClass());
    if (getRepository().getOneByPublicIdIfExists(entity.getId()) != null) {
      throw new ClientErrorException(Response.Status.CONFLICT);
    }
    return getRepository().save(entity);
  }

  public Response createEntity(D dto, UriInfo uriInfo) {
    return buildCreatedResponse(uriInfo, createEntity(dto));
  }

  public E updateEntity(String id, D dto) {
    E entity = getExisting(id);
    getValidator().validateUpdate(dto, entity);
    return getRepository().save(getMapper().updateFromDTO(dto, entity));
  }

  public D getEntity(String id) {
    E entity = getExisting(id);
    return getMapper().toDTO(entity, true);
  }

  public E deleteEntity(String id) {
    E entity = getExisting(id);
    getValidator().validateDelete(entity);
    getRepository().delete(entity);
    return entity;
  }

  public List<D> listAllEntities() {
    return listAllEntities(false);
  }

  protected List<D> listAllEntities(boolean fullDetails) {
    return getRepository().findAll().stream().map(r -> getMapper().toDTO(r, fullDetails)).toList();
  }

  protected Response buildCreatedResponse(UriInfo uriInfo, E entity) {
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    builder.path(entity.getId());
    return Response.created(builder.build()).build();
  }

  protected E getExisting(String id) {
    try {
      return getRepository().getOneByPublicId(id);
    } catch (DataRetrievalFailureException e) {
      throw new NotFoundException(
        getEntityClass().getSimpleName() + " with id: [" + id + "] not found"
      );
    }
  }
}
