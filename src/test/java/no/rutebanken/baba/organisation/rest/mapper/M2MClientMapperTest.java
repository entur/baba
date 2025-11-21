package no.rutebanken.baba.organisation.rest.mapper;

import static no.rutebanken.baba.organisation.model.user.M2MClient.INTERNAL_ISSUER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import org.junit.jupiter.api.Test;

class M2MClientMapperTest {

  public static final String TEST_CLIENT_NAME = "Test M2MClient name";
  public static final String TEST_CLIENT_PRIVATE_CODE = "Test M2MClient private code";
  public static final long TEST_ENTUR_ORGANISATION_ID = 1L;

  @Test
  void toM2MClientDTO() {
    M2MClient m2MClient = new M2MClient();
    m2MClient.setName(TEST_CLIENT_NAME);
    m2MClient.setPrivateCode(TEST_CLIENT_PRIVATE_CODE);
    m2MClient.setEnturOrganisationId(TEST_ENTUR_ORGANISATION_ID);
    m2MClient.setIssuer(INTERNAL_ISSUER);

    M2MClientMapper mapper = new M2MClientMapper(null, null);
    M2MClientDTO dto = mapper.toDTO(m2MClient, false);
    assertEquals(TEST_CLIENT_NAME, dto.name);
    assertEquals(TEST_CLIENT_PRIVATE_CODE, dto.privateCode);
    assertEquals(TEST_ENTUR_ORGANISATION_ID, dto.enturOrganisationId);
    assertEquals(INTERNAL_ISSUER, dto.issuer);
    assertTrue(dto.responsibilitySetRefs.isEmpty());
    assertTrue(dto.responsibilitySets.isEmpty());
  }

  @Test
  void toM2MClient() {
    M2MClientDTO dto = new M2MClientDTO();
    dto.name = TEST_CLIENT_NAME;
    dto.privateCode = TEST_CLIENT_PRIVATE_CODE;
    dto.enturOrganisationId = TEST_ENTUR_ORGANISATION_ID;
    dto.issuer = INTERNAL_ISSUER;
    M2MClientMapper mapper = new M2MClientMapper(null, null);
    M2MClient m2MClient = mapper.createFromDTO(dto, M2MClient.class);
    assertEquals(TEST_CLIENT_NAME, m2MClient.getName());
    assertEquals(TEST_CLIENT_PRIVATE_CODE, m2MClient.getPrivateCode());
    assertEquals(TEST_ENTUR_ORGANISATION_ID, m2MClient.getEnturOrganisationId());
    assertEquals(INTERNAL_ISSUER, m2MClient.getIssuer());
    assertTrue(m2MClient.getResponsibilitySets().isEmpty());
  }
}
