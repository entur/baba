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

package no.rutebanken.baba.organisation.rest.mapper;

import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.rest.dto.CodeSpaceDTO;
import org.springframework.stereotype.Service;

@Service
public class CodeSpaceMapper implements DTOMapper<CodeSpace, CodeSpaceDTO> {

  @Override
  public CodeSpace createFromDTO(CodeSpaceDTO dto, Class<CodeSpace> clazz) {
    CodeSpace entity = new CodeSpace();
    entity.setPrivateCode(dto.privateCode);
    entity.setXmlns(dto.xmlns);
    return updateFromDTO(dto, entity);
  }

  @Override
  public CodeSpace updateFromDTO(CodeSpaceDTO dto, CodeSpace entity) {
    entity.setXmlnsUrl(dto.xmlnsUrl);
    return entity;
  }

  @Override
  public CodeSpaceDTO toDTO(CodeSpace entity, boolean fullDetails) {
    return new CodeSpaceDTO(entity.getId(), entity.getXmlns(), entity.getXmlnsUrl());
  }
}
