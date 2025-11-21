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

import jakarta.ws.rs.BadRequestException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import no.rutebanken.baba.organisation.model.organisation.OrganisationPart;
import no.rutebanken.baba.organisation.repository.AdministrativeZoneRepository;
import no.rutebanken.baba.organisation.repository.CodeSpaceRepository;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationDTO;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationPartDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class OrganisationMapper implements DTOMapper<Organisation, OrganisationDTO> {

  protected final CodeSpaceRepository codeSpaceRepository;

  private final AdministrativeZoneRepository administrativeZoneRepository;

  public OrganisationMapper(
    CodeSpaceRepository codeSpaceRepository,
    AdministrativeZoneRepository administrativeZoneRepository
  ) {
    this.codeSpaceRepository = codeSpaceRepository;
    this.administrativeZoneRepository = administrativeZoneRepository;
  }

  public OrganisationDTO toDTO(Organisation entity, boolean fullDetails) {
    OrganisationDTO dto = new OrganisationDTO();
    dto.id = entity.getId();
    dto.privateCode = entity.getPrivateCode();
    dto.codeSpace = entity.getCodeSpace().getId();
    dto.companyNumber = entity.getCompanyNumber();
    dto.name = entity.getName();

    if (entity instanceof Authority) {
      dto.organisationType = OrganisationDTO.OrganisationType.AUTHORITY;
    }

    if (!CollectionUtils.isEmpty(entity.getParts())) {
      dto.parts = entity.getParts().stream().map(this::toDTO).toList();
    }

    return dto;
  }

  @Override
  public Organisation createFromDTO(OrganisationDTO dto, Class<Organisation> clazz) {
    Organisation entity = createByType(dto.organisationType);
    entity.setCodeSpace(codeSpaceRepository.getOneByPublicId(dto.codeSpace));
    entity.setPrivateCode(dto.privateCode);
    return updateFromDTO(dto, entity);
  }

  @Override
  public Organisation updateFromDTO(OrganisationDTO dto, Organisation entity) {
    entity.setName(dto.name);
    entity.setCompanyNumber(dto.companyNumber);

    if (dto.parts == null) {
      entity.getParts().clear();
    } else {
      mergeParts(dto, entity);
    }

    return entity;
  }

  protected void mergeParts(OrganisationDTO dto, Organisation entity) {
    Set<OrganisationPart> removed = new HashSet<>(entity.getParts());
    for (OrganisationPartDTO dtoPart : dto.parts) {
      if (dtoPart.id != null) {
        OrganisationPart part = entity.getOrganisationPart(dtoPart.id);
        removed.remove(part);
        fromDTO(dtoPart, part);
      } else {
        entity.getParts().add(fromDTO(dtoPart, entity.getCodeSpace()));
      }
    }
    entity.getParts().removeAll(removed);
  }

  private OrganisationPartDTO toDTO(OrganisationPart part) {
    OrganisationPartDTO dto = new OrganisationPartDTO();
    dto.name = part.getName();
    dto.id = part.getId();
    if (!CollectionUtils.isEmpty(part.getAdministrativeZones())) {
      dto.administrativeZoneRefs =
        part.getAdministrativeZones().stream().map(CodeSpaceEntity::getId).toList();
    }

    return dto;
  }

  private OrganisationPart fromDTO(OrganisationPartDTO dto, CodeSpace codeSpace) {
    OrganisationPart entity = new OrganisationPart();

    entity.setCodeSpace(codeSpace);
    entity.setPrivateCode(UUID.randomUUID().toString());

    return fromDTO(dto, entity);
  }

  private OrganisationPart fromDTO(OrganisationPartDTO dto, OrganisationPart entity) {
    entity.setName(dto.name);
    if (!CollectionUtils.isEmpty(dto.administrativeZoneRefs)) {
      entity.setAdministrativeZones(
        dto.administrativeZoneRefs
          .stream()
          .map(administrativeZoneRepository::getOneByPublicId)
          .collect(Collectors.toSet())
      );
    } else {
      entity.setAdministrativeZones(new HashSet<>());
    }
    return entity;
  }

  private Organisation createByType(OrganisationDTO.OrganisationType type) {
    if (type == OrganisationDTO.OrganisationType.AUTHORITY) {
      return new Authority();
    }
    throw new BadRequestException("Unknown organisation type:" + type);
  }
}
