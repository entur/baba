package no.rutebanken.baba.organisation.rest.validation;

import no.rutebanken.baba.organisation.model.user.M2MClient;
import no.rutebanken.baba.organisation.rest.dto.m2m.M2MClientDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class M2MClientValidatorTest {

    @Test
    void validateInvalidDTO() {
        M2MClientValidator validator = new M2MClientValidator();
        M2MClientDTO dto = new M2MClientDTO();
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void validateValidDTO() {
        M2MClientValidator validator = new M2MClientValidator();
        M2MClientDTO dto = new M2MClientDTO();
        dto.privateCode = "testPivateCode";
        dto.name = "testName";
        dto.issuer = M2MClient.INTERNAL_ISSUER;
        dto.enturOrganisationId = 1L;
        validator.validateCreate(dto);
    }

}