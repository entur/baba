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
import java.util.UUID;
import java.util.stream.Collectors;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.ContactDetails;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.OrganisationRepository;
import no.rutebanken.baba.organisation.repository.ResponsibilitySetRepository;
import no.rutebanken.baba.organisation.rest.dto.user.ContactDetailsDTO;
import no.rutebanken.baba.organisation.rest.dto.user.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UserMapper implements DTOMapper<User, UserDTO> {

  private final OrganisationRepository organisationRepository;

  private final ResponsibilitySetRepository responsibilitySetRepository;

  private final OrganisationMapper organisationMapper;

  private final ResponsibilitySetMapper responsibilitySetMapper;

  private final NotificationConfigurationMapper notificationConfigurationMapper;

  public UserMapper(
    OrganisationRepository organisationRepository,
    ResponsibilitySetRepository responsibilitySetRepository,
    OrganisationMapper organisationMapper,
    ResponsibilitySetMapper responsibilitySetMapper,
    NotificationConfigurationMapper notificationConfigurationMapper
  ) {
    this.organisationRepository = organisationRepository;
    this.responsibilitySetRepository = responsibilitySetRepository;
    this.organisationMapper = organisationMapper;
    this.responsibilitySetMapper = responsibilitySetMapper;
    this.notificationConfigurationMapper = notificationConfigurationMapper;
  }

  public UserDTO toDTO(User org, boolean fullDetails) {
    UserDTO dto = new UserDTO();
    dto.id = org.getId();
    dto.username = org.getUsername();
    dto.personalAccount = org.isPersonalAccount();

    dto.contactDetails = toDTO(org.getContactDetails());

    dto.responsibilitySetRefs = toRefList(org.getResponsibilitySets());
    dto.organisationRef = org.getOrganisation().getId();

    if (fullDetails) {
      dto.notifications =
        notificationConfigurationMapper.toDTO(org.getNotificationConfigurations(), fullDetails);
      dto.organisation = organisationMapper.toDTO(org.getOrganisation(), false);
      dto.responsibilitySets =
        org
          .getResponsibilitySets()
          .stream()
          .map(rs -> responsibilitySetMapper.toDTO(rs, false))
          .toList();
    }

    return dto;
  }

  public User createFromDTO(UserDTO dto, Class<User> clazz) {
    User entity = new User();
    entity.setPrivateCode(UUID.randomUUID().toString());
    entity.setUsername(dto.username.toLowerCase());

    return updateFromDTO(dto, entity);
  }

  public User updateFromDTO(UserDTO dto, User entity) {
    entity.setPersonalAccount(dto.personalAccount);

    entity.setContactDetails(fromDTO(dto.contactDetails));

    if (dto.organisationRef != null) {
      entity.setOrganisation(organisationRepository.getOneByPublicId(dto.organisationRef));
    }
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

  private ContactDetailsDTO toDTO(ContactDetails entity) {
    if (entity == null) {
      return null;
    }
    ContactDetailsDTO dto = new ContactDetailsDTO();
    dto.email = entity.getEmail();
    dto.firstName = entity.getFirstName();
    dto.lastName = entity.getLastName();
    dto.phone = entity.getPhone();
    return dto;
  }

  private ContactDetails fromDTO(ContactDetailsDTO dto) {
    if (dto == null) {
      return null;
    }
    ContactDetails entity = new ContactDetails();
    entity.setFirstName(dto.firstName);
    entity.setLastName(dto.lastName);
    entity.setEmail(dto.email);
    entity.setPhone(dto.phone);
    return entity;
  }

  private List<String> toRefList(Set<ResponsibilitySet> responsibilitySetSet) {
    if (responsibilitySetSet == null) {
      return List.of();
    }
    return responsibilitySetSet.stream().map(CodeSpaceEntity::getId).toList();
  }
}
