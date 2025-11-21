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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.repository.ResponsibilitySetRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilitySetDTO;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.mapper.ResponsibilitySetMapper;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import no.rutebanken.baba.organisation.rest.validation.ResponsibilitySetValidator;
import no.rutebanken.baba.organisation.service.IamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("responsibility_sets")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(
  value = { @Tag(name = "ResponsibilitySetResource", description = "Responsibility set resource") }
)
public class ResponsibilitySetResource
  extends AnnotatedBaseResource<ResponsibilitySet, ResponsibilitySetDTO> {

  private final ResponsibilitySetRepository repository;

  private final ResponsibilitySetMapper mapper;

  private final ResponsibilitySetValidator validator;

  private final IamService iamService;

  public ResponsibilitySetResource(
    ResponsibilitySetRepository repository,
    ResponsibilitySetMapper mapper,
    ResponsibilitySetValidator validator,
    IamService iamService
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validator = validator;
    this.iamService = iamService;
  }

  @Override
  @PUT
  @Path("{id}")
  public void update(@PathParam("id") String id, ResponsibilitySetDTO dto) {
    ResponsibilitySet entity = updateEntity(id, dto);
    iamService.updateResponsibilitySet(entity);
  }

  @Override
  protected Class<ResponsibilitySet> getEntityClass() {
    return ResponsibilitySet.class;
  }

  @Override
  protected VersionedEntityRepository<ResponsibilitySet> getRepository() {
    return repository;
  }

  @Override
  protected DTOMapper<ResponsibilitySet, ResponsibilitySetDTO> getMapper() {
    return mapper;
  }

  @Override
  protected DTOValidator<ResponsibilitySet, ResponsibilitySetDTO> getValidator() {
    return validator;
  }
}
