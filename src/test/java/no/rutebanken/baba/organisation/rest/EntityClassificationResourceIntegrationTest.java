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

import java.net.URI;
import java.util.Arrays;
import no.rutebanken.baba.organisation.TestConstantsOrganisation;
import no.rutebanken.baba.organisation.repository.BaseIntegrationTest;
import no.rutebanken.baba.organisation.rest.dto.TypeDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EntityClassificationResourceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private static final String PATH =
    "/services/organisations/entity_types/" +
    TestConstantsOrganisation.ENTITY_TYPE_ID +
    "/entity_classifications";

  @Test
  void entityClassificationNotFound() {
    ResponseEntity<TypeDTO> entity = restTemplate.getForEntity(
      PATH + "/unknownEntityClassifications",
      TypeDTO.class
    );
    Assertions.assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  @Test
  void crudEntityClassification() {
    TypeDTO createEntityClassification = createEntityClassification(
      "entityClassification name",
      "privateCode"
    );
    URI uri = restTemplate.postForLocation(PATH, createEntityClassification);
    ResourceTestUtils.assertType(createEntityClassification, uri, restTemplate);

    TypeDTO updateEntityClassification = createEntityClassification(
      "new name",
      createEntityClassification.privateCode
    );
    restTemplate.put(uri, updateEntityClassification);
    ResourceTestUtils.assertType(updateEntityClassification, uri, restTemplate);

    TypeDTO[] allEntityClassifications = restTemplate.getForObject(PATH, TypeDTO[].class);
    assertEntityClassificationInArray(updateEntityClassification, allEntityClassifications);

    restTemplate.delete(uri);

    ResponseEntity<TypeDTO> entity = restTemplate.getForEntity(uri, TypeDTO.class);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  private void assertEntityClassificationInArray(TypeDTO entityClassification, TypeDTO[] array) {
    Assertions.assertNotNull(array);
    Assertions.assertTrue(
      Arrays.stream(array).anyMatch(r -> r.privateCode.equals(entityClassification.privateCode))
    );
  }

  protected TypeDTO createEntityClassification(String name, String privateCode) {
    TypeDTO entityClassification = new TypeDTO();
    entityClassification.name = name;
    entityClassification.privateCode = privateCode;
    entityClassification.codeSpace = TestConstantsOrganisation.CODE_SPACE_ID;
    return entityClassification;
  }

  @Test
  void createInvalidEntityClassification() {
    TypeDTO inEntityClassification = createEntityClassification("entityClassification name", null);
    ResponseEntity<String> rsp = restTemplate.postForEntity(
      PATH,
      inEntityClassification,
      String.class
    );

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }
}
