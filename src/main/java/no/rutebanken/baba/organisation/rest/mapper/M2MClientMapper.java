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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.ResponsibilitySetRepository;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class M2MClientMapper implements DTOMapper<M2MClient, M2MClientDTO> {

  private final ResponsibilitySetRepository responsibilitySetRepository;
  private final ResponsibilitySetMapper responsibilitySetMapper;

  public M2MClientMapper(
    ResponsibilitySetRepository responsibilitySetRepository,
    ResponsibilitySetMapper responsibilitySetMapper
  ) {
    this.responsibilitySetRepository = responsibilitySetRepository;
    this.responsibilitySetMapper = responsibilitySetMapper;
  }

  @Override
  public M2MClient createFromDTO(M2MClientDTO dto, Class<M2MClient> clazz) {
    M2MClient entity = new M2MClient();
    entity.setPrivateCode(dto.privateCode);
    return updateFromDTO(dto, entity);
  }

  @Override
  public M2MClient updateFromDTO(M2MClientDTO dto, M2MClient entity) {
    entity.setName(dto.name);
    entity.setIssuer(dto.issuer);
    entity.setEnturOrganisationId(dto.enturOrganisationId);
    if (CollectionUtils.isEmpty(dto.responsibilitySetRefs)) {
      entity.setResponsibilitySets(new HashSet<>());
    } else {
      entity.setResponsibilitySets(
        dto.responsibilitySetRefs
          .stream()
          .map(responsibilitySetRepository::getOneByPublicId)
          .collect(Collectors.toSet())
      );
    }

    return entity;
  }

  @Override
  public M2MClientDTO toDTO(M2MClient entity, boolean fullDetails) {
    M2MClientDTO dto = new M2MClientDTO();
    dto.name = entity.getName();
    dto.privateCode = entity.getPrivateCode();
    dto.enturOrganisationId = entity.getEnturOrganisationId();
    dto.issuer = entity.getIssuer();
    dto.responsibilitySetRefs = toRefList(entity.getResponsibilitySets());

    if (fullDetails) {
      dto.responsibilitySets =
        entity
          .getResponsibilitySets()
          .stream()
          .map(rs -> responsibilitySetMapper.toDTO(rs, false))
          .toList();
    } else {
      dto.responsibilitySets = List.of();
    }

    return dto;
  }

  private List<String> toRefList(Set<ResponsibilitySet> responsibilitySetSet) {
    if (responsibilitySetSet == null) {
      return List.of();
    }
    return responsibilitySetSet.stream().map(CodeSpaceEntity::getId).toList();
  }
}
