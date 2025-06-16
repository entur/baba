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

package no.rutebanken.baba.organisation.rest.dto.responsibility;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(
  description = "Describes whether a user is explicitly authorized / not authorized to do something to a given entity classification "
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityClassificationAssignmentDTO {

  public boolean allow = true;

  public String entityClassificationRef;

  public EntityClassificationAssignmentDTO() {}

  public EntityClassificationAssignmentDTO(String entityClassificationRef, boolean allow) {
    this.allow = allow;
    this.entityClassificationRef = entityClassificationRef;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EntityClassificationAssignmentDTO that = (EntityClassificationAssignmentDTO) o;

    if (allow != that.allow) {
      return false;
    }
    return Objects.equals(entityClassificationRef, that.entityClassificationRef);
  }

  @Override
  public int hashCode() {
    int result = (allow ? 1 : 0);
    result =
      31 * result + (entityClassificationRef != null ? entityClassificationRef.hashCode() : 0);
    return result;
  }
}
