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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.responsibility.EntityClassification;
import no.rutebanken.baba.organisation.model.user.NotificationConfiguration;
import no.rutebanken.baba.organisation.model.user.NotificationType;
import no.rutebanken.baba.organisation.model.user.eventfilter.CrudEventFilter;
import no.rutebanken.baba.organisation.model.user.eventfilter.EventFilter;
import no.rutebanken.baba.organisation.model.user.eventfilter.JobEventFilter;
import no.rutebanken.baba.organisation.repository.AdministrativeZoneRepository;
import no.rutebanken.baba.organisation.repository.EntityClassificationRepository;
import no.rutebanken.baba.organisation.repository.OrganisationRepository;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityClassificationDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityTypeDTO;
import no.rutebanken.baba.organisation.rest.dto.user.EventFilterDTO;
import no.rutebanken.baba.organisation.rest.dto.user.NotificationConfigDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class NotificationConfigurationMapper {

  private final EntityClassificationRepository entityClassificationRepository;

  private final AdministrativeZoneRepository administrativeZoneRepository;

  private final OrganisationRepository organisationRepository;

  private final OrganisationMapper organisationMapper;

  public NotificationConfigurationMapper(
    EntityClassificationRepository entityClassificationRepository,
    AdministrativeZoneRepository administrativeZoneRepository,
    OrganisationRepository organisationRepository,
    OrganisationMapper organisationMapper
  ) {
    this.entityClassificationRepository = entityClassificationRepository;
    this.administrativeZoneRepository = administrativeZoneRepository;
    this.organisationRepository = organisationRepository;
    this.organisationMapper = organisationMapper;
  }

  public Set<NotificationConfigDTO> toDTO(
    Collection<NotificationConfiguration> entity,
    boolean fullDetails
  ) {
    if (CollectionUtils.isEmpty(entity)) {
      return new HashSet<>();
    }

    return entity
      .stream()
      .map(n ->
        new NotificationConfigDTO(
          n.getNotificationType(),
          n.isEnabled(),
          toDTO(n.getEventFilter(), fullDetails)
        )
      )
      .collect(Collectors.toSet());
  }

  public Set<NotificationConfiguration> fromDTO(Set<NotificationConfigDTO> dtos) {
    if (CollectionUtils.isEmpty(dtos)) {
      return new HashSet<>();
    }
    return dtos.stream().map(this::fromDTO).collect(Collectors.toSet());
  }

  private EventFilterDTO toDTO(EventFilter eventFilter, boolean fullDetails) {
    EventFilterDTO dto = new EventFilterDTO();

    if (eventFilter instanceof JobEventFilter jobEventFilter) {
      dto.type = EventFilterDTO.EventFilterType.JOB;
      dto.states = jobEventFilter.getStates();
      dto.actions = jobEventFilter.getActions();
      dto.jobDomain = EventFilterDTO.JobDomain.valueOf(jobEventFilter.getJobDomain());
    } else if (eventFilter instanceof CrudEventFilter crudEventFilter) {
      dto.type = EventFilterDTO.EventFilterType.CRUD;
      dto.entityClassificationRefs =
        crudEventFilter
          .getEntityClassifications()
          .stream()
          .map(CodeSpaceEntity::getId)
          .collect(Collectors.toSet());
      if (fullDetails) {
        dto.entityClassifications =
          crudEventFilter
            .getEntityClassifications()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toSet());
      }

      dto.administrativeZoneRefs =
        crudEventFilter
          .getAdministrativeZones()
          .stream()
          .map(CodeSpaceEntity::getId)
          .collect(Collectors.toSet());
    }

    if (eventFilter.getOrganisation() != null) {
      dto.organisationRef = eventFilter.getOrganisation().getId();
      if (fullDetails) {
        dto.organisation = organisationMapper.toDTO(eventFilter.getOrganisation(), fullDetails);
      }
    }
    return dto;
  }

  private EntityClassificationDTO toDTO(EntityClassification entity) {
    EntityClassificationDTO dto = new EntityClassificationDTO();
    dto.codeSpace = entity.getCodeSpace().getPrivateCode();
    dto.id = entity.getId();
    dto.name = entity.getName();
    dto.privateCode = entity.getPrivateCode();

    EntityTypeDTO typeDTO = new EntityTypeDTO();
    typeDTO.codeSpace = dto.codeSpace;
    typeDTO.id = entity.getEntityType().getId();
    typeDTO.name = entity.getEntityType().getName();
    typeDTO.privateCode = entity.getEntityType().getPrivateCode();

    dto.entityType = typeDTO;
    return dto;
  }

  // TODO recreate at every change? or match existing
  private NotificationConfiguration fromDTO(NotificationConfigDTO dto) {
    NotificationConfiguration notificationConfiguration = new NotificationConfiguration();
    notificationConfiguration.setEnabled(dto.enabled);
    notificationConfiguration.setEventFilter(fromDTO(dto.eventFilter));
    notificationConfiguration.setNotificationType(
      NotificationType.valueOf(dto.notificationType.name())
    );
    return notificationConfiguration;
  }

  private EventFilter fromDTO(EventFilterDTO dto) {
    EventFilter eventFilter;
    if (EventFilterDTO.EventFilterType.CRUD.equals(dto.type)) {
      CrudEventFilter crudEventFilter = new CrudEventFilter();
      crudEventFilter.setEntityClassifications(
        dto.entityClassificationRefs
          .stream()
          .map(entityClassificationRepository::getOneByPublicId)
          .collect(Collectors.toSet())
      );
      if (!CollectionUtils.isEmpty(dto.administrativeZoneRefs)) {
        crudEventFilter.setAdministrativeZones(
          dto.administrativeZoneRefs
            .stream()
            .map(administrativeZoneRepository::getOneByPublicId)
            .collect(Collectors.toSet())
        );
      }
      eventFilter = crudEventFilter;
    } else if (EventFilterDTO.EventFilterType.JOB.equals(dto.type)) {
      JobEventFilter jobEventFilter = new JobEventFilter();
      jobEventFilter.setActions(dto.actions);
      jobEventFilter.setStates(dto.states);
      jobEventFilter.setJobDomain(dto.jobDomain.toString());
      eventFilter = jobEventFilter;
    } else {
      throw new IllegalArgumentException("Unknown event filter type: " + dto.type);
    }

    if (dto.organisationRef != null) {
      eventFilter.setOrganisation(organisationRepository.getOneByPublicId(dto.organisationRef));
    }
    return eventFilter;
  }
}
