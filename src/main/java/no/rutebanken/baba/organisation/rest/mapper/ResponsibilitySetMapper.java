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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassification;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassificationAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.repository.AdministrativeZoneRepository;
import no.rutebanken.baba.organisation.repository.CodeSpaceRepository;
import no.rutebanken.baba.organisation.repository.EntityClassificationRepository;
import no.rutebanken.baba.organisation.repository.OrganisationRepository;
import no.rutebanken.baba.organisation.repository.RoleRepository;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityClassificationAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilityRoleAssignmentDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.ResponsibilitySetDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ResponsibilitySetMapper implements DTOMapper<ResponsibilitySet, ResponsibilitySetDTO> {

  private final RoleRepository roleRepository;

  private final OrganisationRepository organisationRepository;

  private final AdministrativeZoneRepository administrativeZoneRepository;

  private final EntityClassificationRepository entityClassificationRepository;

  protected final CodeSpaceRepository codeSpaceRepository;

  public ResponsibilitySetMapper(
    RoleRepository roleRepository,
    OrganisationRepository organisationRepository,
    AdministrativeZoneRepository administrativeZoneRepository,
    EntityClassificationRepository entityClassificationRepository,
    CodeSpaceRepository codeSpaceRepository
  ) {
    this.roleRepository = roleRepository;
    this.organisationRepository = organisationRepository;
    this.administrativeZoneRepository = administrativeZoneRepository;
    this.entityClassificationRepository = entityClassificationRepository;
    this.codeSpaceRepository = codeSpaceRepository;
  }

  public ResponsibilitySet createFromDTO(ResponsibilitySetDTO dto, Class<ResponsibilitySet> clazz) {
    ResponsibilitySet entity = new ResponsibilitySet();
    entity.setPrivateCode(dto.privateCode);
    entity.setCodeSpace(codeSpaceRepository.getOneByPublicId(dto.codeSpace));
    entity.setName(dto.name);

    if (!CollectionUtils.isEmpty(dto.roles)) {
      entity.setRoles(
        dto.roles.stream().map(ra -> fromDTO(ra, entity.getCodeSpace())).collect(Collectors.toSet())
      );
    }

    return entity;
  }

  @Override
  public ResponsibilitySet updateFromDTO(ResponsibilitySetDTO dto, ResponsibilitySet entity) {
    entity.setName(dto.name);

    if (dto.roles == null) {
      entity.getRoles().clear();
    } else {
      mergeRoles(dto, entity);
    }

    return entity;
  }

  protected void mergeRoles(ResponsibilitySetDTO dto, ResponsibilitySet entity) {
    Set<ResponsibilityRoleAssignment> removedAssignments = new HashSet<>(entity.getRoles());
    for (ResponsibilityRoleAssignmentDTO dtoRole : dto.roles) {
      if (dtoRole.id != null) {
        ResponsibilityRoleAssignment assignment = entity.getResponsibilityRoleAssignment(
          dtoRole.id
        );
        removedAssignments.remove(assignment);
        fromDTO(dtoRole, assignment);
      } else {
        entity.getRoles().add(fromDTO(dtoRole, entity.getCodeSpace()));
      }
    }
    entity.getRoles().removeAll(removedAssignments);
  }

  public ResponsibilitySetDTO toDTO(ResponsibilitySet entity, boolean fullDetails) {
    ResponsibilitySetDTO dto = new ResponsibilitySetDTO();
    dto.id = entity.getId();
    dto.privateCode = entity.getPrivateCode();
    dto.codeSpace = entity.getCodeSpace().getId();
    dto.name = entity.getName();
    dto.roles = entity.getRoles().stream().map(this::toDTO).toList();
    return dto;
  }

  private ResponsibilityRoleAssignmentDTO toDTO(ResponsibilityRoleAssignment entity) {
    ResponsibilityRoleAssignmentDTO dto = new ResponsibilityRoleAssignmentDTO();
    dto.id = entity.getId();
    dto.responsibleOrganisationRef = entity.getResponsibleOrganisation().getId();
    dto.typeOfResponsibilityRoleRef = entity.getTypeOfResponsibilityRole().getId();

    if (entity.getResponsibleArea() != null) {
      dto.responsibleAreaRef = entity.getResponsibleArea().getId();
    }
    if (!CollectionUtils.isEmpty(entity.getResponsibleEntityClassifications())) {
      dto.entityClassificationAssignments =
        entity
          .getResponsibleEntityClassifications()
          .stream()
          .map(ec ->
            new EntityClassificationAssignmentDTO(
              ec.getEntityClassification().getId(),
              ec.isAllow()
            )
          )
          .toList();
    }

    return dto;
  }

  private ResponsibilityRoleAssignment fromDTO(
    ResponsibilityRoleAssignmentDTO dto,
    CodeSpace codeSpace
  ) {
    ResponsibilityRoleAssignment entity = new ResponsibilityRoleAssignment();
    entity.setCodeSpace(codeSpace);
    entity.setPrivateCode(UUID.randomUUID().toString());

    return fromDTO(dto, entity);
  }

  private ResponsibilityRoleAssignment fromDTO(
    ResponsibilityRoleAssignmentDTO dto,
    ResponsibilityRoleAssignment entity
  ) {
    entity.setTypeOfResponsibilityRole(
      roleRepository.getOneByPublicId(dto.typeOfResponsibilityRoleRef)
    );
    entity.setResponsibleOrganisation(
      organisationRepository.getOneByPublicId(dto.responsibleOrganisationRef)
    );
    if (dto.responsibleAreaRef != null) {
      entity.setResponsibleArea(
        administrativeZoneRepository.getOneByPublicId(dto.responsibleAreaRef)
      );
    }

    if (CollectionUtils.isEmpty(dto.entityClassificationAssignments)) {
      entity.setResponsibleEntityClassifications(new HashSet<>());
    } else {
      mergeClassifications(dto, entity);
    }

    return entity;
  }

  protected void mergeClassifications(
    ResponsibilityRoleAssignmentDTO dto,
    ResponsibilityRoleAssignment entity
  ) {
    Set<EntityClassificationAssignment> removedClassifications = new HashSet<>(
      entity.getResponsibleEntityClassifications()
    );

    for (EntityClassificationAssignmentDTO dtoClassification : dto.entityClassificationAssignments) {
      EntityClassificationAssignment existingClassification =
        entity.getResponsibleEntityClassification(dtoClassification.entityClassificationRef);

      if (existingClassification != null) {
        removedClassifications.remove(existingClassification);
        existingClassification.setAllow(dtoClassification.allow);
      } else {
        EntityClassification entityClassification = entityClassificationRepository.getOneByPublicId(
          dtoClassification.entityClassificationRef
        );

        entity
          .getResponsibleEntityClassifications()
          .add(
            new EntityClassificationAssignment(
              entityClassification,
              entity,
              dtoClassification.allow
            )
          );
      }
    }
    entity.getResponsibleEntityClassifications().removeAll(removedClassifications);
  }
}
