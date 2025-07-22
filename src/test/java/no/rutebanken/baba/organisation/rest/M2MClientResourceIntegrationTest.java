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

import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.repository.BaseIntegrationTest;
import no.rutebanken.baba.organisation.repository.M2MClientRepository;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class M2MClientResourceIntegrationTest extends BaseIntegrationTest {
    private static final String TEST_PRIVATE_CODE = "testPrivateCode";
    private static final String TEST_OTHER_PRIVATE_CODE = "testOtherPrivateCode";
    private static final String TEST_CLIENT_NAME = "testName";
    private static final long TEST_ENTUR_ORGANISATION_ID = 1L;
    private static final String PATH = "/services/organisations/m2m_clients";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private M2MClientRepository repository;

    @Test
    void clientNotFound() {
        ResponseEntity<M2MClientDTO> entity = restTemplate.getForEntity(PATH + "/unknownUser",
                M2MClientDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    void createClient() {
        M2MClientDTO client = createTestClientDto(TEST_PRIVATE_CODE);
        ResponseEntity<String> rsp = restTemplate.postForEntity(PATH, client, String.class);
        assertEquals(HttpStatus.CREATED, rsp.getStatusCode());
        assertNotNull(repository.getOneByPublicId(TEST_PRIVATE_CODE));
    }


    @Test
    void updateClient() {
        M2MClientDTO client = createTestClientDto(TEST_OTHER_PRIVATE_CODE);
        restTemplate.postForEntity(PATH, client, String.class);

        M2MClientDTO dto = createTestClientDto(TEST_OTHER_PRIVATE_CODE);
        dto.privateCode = null;
        dto.issuer = M2MClient.PARTNER_ISSUER;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> update = restTemplate.exchange(PATH + "/" + TEST_OTHER_PRIVATE_CODE, HttpMethod.PUT, new HttpEntity<>(dto, headers), String.class);
        assertEquals(HttpStatus.NO_CONTENT, update.getStatusCode());
        M2MClient updateClient = repository.getOneByPublicId(TEST_OTHER_PRIVATE_CODE);
        assertNotNull(M2MClient.PARTNER_ISSUER, updateClient.getIssuer());

    }

    @Test
    void createInvalidClient() {
        M2MClientDTO client = createTestClientDto(TEST_PRIVATE_CODE);
        client.issuer = "unknown issuer";
        ResponseEntity<String> rsp = restTemplate.postForEntity(PATH, client, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
    }


    private static M2MClientDTO createTestClientDto(String privateCode) {
        M2MClientDTO client = new M2MClientDTO();
        client.privateCode = privateCode;
        client.name = TEST_CLIENT_NAME;
        client.enturOrganisationId = TEST_ENTUR_ORGANISATION_ID;
        client.issuer = M2MClient.INTERNAL_ISSUER;
        return client;
    }


}
