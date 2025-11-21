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
import no.rutebanken.baba.organisation.model.responsibility.EntityClassification;
import no.rutebanken.baba.organisation.model.responsibility.EntityType;
import no.rutebanken.baba.organisation.repository.CodeSpaceRepository;
import no.rutebanken.baba.organisation.rest.dto.TypeDTO;
import no.rutebanken.baba.organisation.rest.dto.responsibility.EntityTypeDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EntityTypeMapper implements DTOMapper<EntityType, EntityTypeDTO> {

  private final TypeMapper<EntityClassification> classificationTypeMapper;

  protected final CodeSpaceRepository codeSpaceRepository;

  public EntityTypeMapper(
    TypeMapper<EntityClassification> classificationTypeMapper,
    CodeSpaceRepository codeSpaceRepository
  ) {
    this.classificationTypeMapper = classificationTypeMapper;
    this.codeSpaceRepository = codeSpaceRepository;
  }

  @Override
  public EntityType createFromDTO(EntityTypeDTO dto, Class<EntityType> clazz) {
    EntityType entity = new EntityType();
    entity.setPrivateCode(dto.privateCode);
    entity.setCodeSpace(codeSpaceRepository.getOneByPublicId(dto.codeSpace));

    return updateFromDTO(dto, entity);
  }

  @Override
  public EntityType updateFromDTO(EntityTypeDTO dto, EntityType entity) {
    entity.setName(dto.name);

    if (CollectionUtils.isEmpty(dto.classifications)) {
      entity.setClassifications(new HashSet<>());
    } else {
      mergeClassifications(dto, entity);
    }

    return entity;
  }

  @Override
  public EntityTypeDTO toDTO(EntityType entity, boolean fullDetails) {
    EntityTypeDTO dto = new EntityTypeDTO();
    dto.name = entity.getName();
    dto.id = entity.getId();
    dto.privateCode = entity.getPrivateCode();
    dto.codeSpace = entity.getCodeSpace().getId();

    dto.classifications =
      entity
        .getClassifications()
        .stream()
        .map(ec -> classificationTypeMapper.toDTO(ec, false))
        .toList();
    return dto;
  }

  protected void mergeClassifications(EntityTypeDTO dto, EntityType entity) {
    Set<EntityClassification> removedClassifications = new HashSet<>(entity.getClassifications());

    for (TypeDTO dtoClassification : dto.classifications) {
      EntityClassification existingClassification = entity.getClassification(
        dtoClassification.privateCode
      );

      if (existingClassification != null) {
        removedClassifications.remove(existingClassification);
        classificationTypeMapper.updateFromDTO(dtoClassification, existingClassification);
      } else {
        dtoClassification.codeSpace = entity.getCodeSpace().getId();
        EntityClassification newClassification = classificationTypeMapper.createFromDTO(
          dtoClassification,
          EntityClassification.class
        );
        newClassification.setEntityType(entity);
        entity.getClassifications().add(newClassification);
      }
    }
    entity.getClassifications().removeAll(removedClassifications);
  }
}
