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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import no.rutebanken.baba.organisation.model.responsibility.EntityType;
import no.rutebanken.baba.organisation.repository.EntityTypeRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityTypeDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.mapper.EntityTypeMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import no.rutebanken.baba.organisation.rest.validation.EntityTypeValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("entity_types")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(value = { @Tag(name = "EntityTypeResource", description = "Entity type resource") })
public class EntityTypeResource extends AnnotatedBaseResource<EntityType, EntityTypeDTO> {

  private final EntityTypeRepository repository;
  private final EntityTypeMapper mapper;
  private final EntityTypeValidator validator;

  public EntityTypeResource(
    EntityTypeRepository repository,
    EntityTypeMapper mapper,
    EntityTypeValidator validator
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validator = validator;
  }

  @Override
  protected Class<EntityType> getEntityClass() {
    return EntityType.class;
  }

  @Override
  protected VersionedEntityRepository<EntityType> getRepository() {
    return repository;
  }

  @Override
  protected DTOMapper<EntityType, EntityTypeDTO> getMapper() {
    return mapper;
  }

  @Override
  protected DTOValidator<EntityType, EntityTypeDTO> getValidator() {
    return validator;
  }
}
