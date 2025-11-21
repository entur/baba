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
import java.util.ArrayList;
import java.util.Arrays;
import no.rutebanken.baba.organisation.TestConstantsOrganisation;
import no.rutebanken.baba.organisation.repository.BaseIntegrationTest;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationDTO;
import no.rutebanken.baba.organisation.rest.dto.organisation.OrganisationPartDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

class OrganisationResourceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private static final String PATH = "/services/organisations";

  @Test
  void organisationNotFound() {
    ResponseEntity<OrganisationDTO> entity = restTemplate.getForEntity(
      PATH + "/unknownOrganisation",
      OrganisationDTO.class
    );
    Assertions.assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  @Test
  void crudOrganisation() {
    OrganisationDTO createOrganisation = createOrganisation("TheOrg", "Org name", null);
    URI uri = restTemplate.postForLocation(PATH, createOrganisation);
    assertOrganisation(createOrganisation, uri);

    OrganisationPartDTO orgPart1 = new OrganisationPartDTO();
    orgPart1.name = "part 1";

    OrganisationDTO updateOrganisation = createOrganisation(
      createOrganisation.privateCode,
      "newOrg name",
      2L,
      orgPart1
    );
    restTemplate.put(uri, updateOrganisation);
    assertOrganisation(updateOrganisation, uri);

    OrganisationDTO[] allOrganisations = restTemplate.getForObject(PATH, OrganisationDTO[].class);
    assertOrganisationInArray(updateOrganisation, allOrganisations);

    restTemplate.delete(uri);

    ResponseEntity<OrganisationDTO> entity = restTemplate.getForEntity(uri, OrganisationDTO.class);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
  }

  @Test
  void updateOrganisationParts() {
    OrganisationPartDTO orgPart1 = new OrganisationPartDTO();
    orgPart1.name = "part 1";
    orgPart1.administrativeZoneRefs = ResourceTestUtils.addAdminZones(restTemplate, "amd1", "adm2");

    OrganisationPartDTO orgPart2 = new OrganisationPartDTO();
    orgPart2.name = "part2";

    OrganisationDTO organisation = createOrganisation(
      "OrgWithParts",
      "Org name",
      null,
      orgPart1,
      orgPart2
    );
    URI uri = restTemplate.postForLocation(PATH, organisation);
    assertOrganisation(organisation, uri);

    orgPart1.administrativeZoneRefs.removeFirst();
    orgPart1.administrativeZoneRefs.addAll(ResourceTestUtils.addAdminZones(restTemplate, "adm3"));

    restTemplate.put(uri, organisation);
    assertOrganisation(organisation, uri);
    organisation.parts.remove(orgPart2);

    OrganisationPartDTO orgPart3 = new OrganisationPartDTO();
    orgPart3.name = "part3";
    organisation.parts.add(orgPart3);

    restTemplate.put(uri, organisation);
    assertOrganisation(organisation, uri);

    organisation.parts = null;
    restTemplate.put(uri, organisation);
    assertOrganisation(organisation, uri);
  }

  private void assertOrganisationInArray(OrganisationDTO organisation, OrganisationDTO[] array) {
    Assertions.assertNotNull(array);
    Assertions.assertTrue(
      Arrays.stream(array).anyMatch(r -> r.privateCode.equals(organisation.privateCode))
    );
  }

  protected OrganisationDTO createOrganisation(
    String privateCode,
    String name,
    Long companyNumber,
    OrganisationPartDTO... parts
  ) {
    OrganisationDTO organisation = new OrganisationDTO();
    organisation.organisationType = OrganisationDTO.OrganisationType.AUTHORITY;
    organisation.codeSpace = TestConstantsOrganisation.CODE_SPACE_ID;
    organisation.privateCode = privateCode;
    organisation.name = name;
    organisation.companyNumber = companyNumber;
    if (parts != null) {
      organisation.parts = new ArrayList<>(Arrays.asList(parts));
    }

    return organisation;
  }

  protected void assertOrganisation(OrganisationDTO inOrganisation, URI uri) {
    Assertions.assertNotNull(uri);
    ResponseEntity<OrganisationDTO> rsp = restTemplate.getForEntity(uri, OrganisationDTO.class);
    OrganisationDTO outOrganisation = rsp.getBody();
    Assertions.assertEquals(inOrganisation.name, outOrganisation.name);
    Assertions.assertEquals(inOrganisation.privateCode, outOrganisation.privateCode);
    Assertions.assertEquals(inOrganisation.companyNumber, outOrganisation.companyNumber);

    if (CollectionUtils.isEmpty(inOrganisation.parts)) {
      Assertions.assertTrue(CollectionUtils.isEmpty(outOrganisation.parts));
    } else {
      Assertions.assertEquals(inOrganisation.parts.size(), outOrganisation.parts.size());
      for (OrganisationPartDTO in : inOrganisation.parts) {
        Assertions.assertTrue(outOrganisation.parts.stream().anyMatch(out -> isEqual(in, out)));
      }
    }
  }

  private boolean isEqual(OrganisationPartDTO in, OrganisationPartDTO out) {
    if (!in.name.equals(out.name)) {
      return false;
    }

    if (CollectionUtils.isEmpty(in.administrativeZoneRefs)) {
      return CollectionUtils.isEmpty(out.administrativeZoneRefs);
    }

    if (in.administrativeZoneRefs.size() != out.administrativeZoneRefs.size()) {
      return false;
    }
    return in.administrativeZoneRefs.containsAll(out.administrativeZoneRefs);
  }

  @Test
  void createInvalidOrganisation() {
    OrganisationPartDTO partWithoutName = new OrganisationPartDTO();
    OrganisationDTO inOrganisation = createOrganisation(
      "privateCode",
      "organisation name",
      null,
      partWithoutName
    );
    ResponseEntity<String> rsp = restTemplate.postForEntity(PATH, inOrganisation, String.class);

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }

  @Test
  void createOrgWithExistingPrivateCode() {
    OrganisationDTO inOrganisation = createOrganisation(
      "OrgPrivateCode",
      "organisation name",
      null
    );
    ResponseEntity<String> firstRsp = restTemplate.postForEntity(
      PATH,
      inOrganisation,
      String.class
    );

    Assertions.assertEquals(HttpStatus.CREATED, firstRsp.getStatusCode());

    ResponseEntity<String> secondRsp = restTemplate.postForEntity(
      PATH,
      inOrganisation,
      String.class
    );
    Assertions.assertEquals(HttpStatus.CONFLICT, secondRsp.getStatusCode());
  }
}
