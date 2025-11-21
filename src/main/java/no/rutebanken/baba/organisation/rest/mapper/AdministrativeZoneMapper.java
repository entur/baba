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
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.repository.CodeSpaceRepository;
import no.rutebanken.baba.organisation.rest.dto.organisation.AdministrativeZoneDTO;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

@Service
public class AdministrativeZoneMapper
  implements DTOMapper<AdministrativeZone, AdministrativeZoneDTO> {

  private final GeoJSONWriter writer = new GeoJSONWriter();
  private final GeoJSONReader reader = new GeoJSONReader();

  protected final CodeSpaceRepository codeSpaceRepository;

  public AdministrativeZoneMapper(CodeSpaceRepository codeSpaceRepository) {
    this.codeSpaceRepository = codeSpaceRepository;
  }

  public AdministrativeZoneDTO toDTO(AdministrativeZone entity, boolean fullDetails) {
    AdministrativeZoneDTO dto = new AdministrativeZoneDTO();
    dto.id = entity.getId();
    dto.name = entity.getName();
    dto.privateCode = entity.getPrivateCode();
    dto.codeSpace = entity.getCodeSpace().getId();
    dto.type = entity.getAdministrativeZoneType();
    dto.source = entity.getSource();

    if (fullDetails) {
      dto.polygon = (org.wololo.geojson.Polygon) writer.write(entity.getPolygon());
    }
    return dto;
  }

  @Override
  public AdministrativeZone createFromDTO(
    AdministrativeZoneDTO dto,
    Class<AdministrativeZone> clazz
  ) {
    AdministrativeZone entity = new AdministrativeZone();
    entity.setPrivateCode(dto.privateCode);
    entity.setCodeSpace(codeSpaceRepository.getOneByPublicId(dto.codeSpace));
    return updateFromDTO(dto, entity);
  }

  @Override
  public AdministrativeZone updateFromDTO(AdministrativeZoneDTO dto, AdministrativeZone entity) {
    Geometry geometry = reader.read(dto.polygon);
    if (!(geometry instanceof Polygon)) {
      throw new BadRequestException("Polygon is not a valid polygon");
    }
    entity.setPolygon((Polygon) geometry);
    entity.setAdministrativeZoneType(dto.type);
    entity.setName(dto.name);
    entity.setSource(dto.source);
    return entity;
  }
}
