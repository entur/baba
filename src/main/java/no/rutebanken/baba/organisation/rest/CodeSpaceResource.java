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
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.repository.CodeSpaceRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.CodeSpaceDTO;
import no.rutebanken.baba.organisation.rest.mapper.CodeSpaceMapper;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.validation.CodeSpaceValidator;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Produces("application/json")
@Path("code_spaces")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(value = { @Tag(name = "CodeSpaceResource", description = "Code space resource") })
public class CodeSpaceResource extends AnnotatedBaseResource<CodeSpace, CodeSpaceDTO> {

  private final CodeSpaceRepository repository;

  private final CodeSpaceValidator validator;

  private final CodeSpaceMapper mapper;

  public CodeSpaceResource(
    CodeSpaceRepository repository,
    CodeSpaceValidator validator,
    CodeSpaceMapper mapper
  ) {
    this.repository = repository;
    this.validator = validator;
    this.mapper = mapper;
  }

  @Override
  protected VersionedEntityRepository<CodeSpace> getRepository() {
    return repository;
  }

  @Override
  protected DTOMapper<CodeSpace, CodeSpaceDTO> getMapper() {
    return mapper;
  }

  @Override
  protected Class<CodeSpace> getEntityClass() {
    return CodeSpace.class;
  }

  @Override
  protected DTOValidator<CodeSpace, CodeSpaceDTO> getValidator() {
    return validator;
  }
}
