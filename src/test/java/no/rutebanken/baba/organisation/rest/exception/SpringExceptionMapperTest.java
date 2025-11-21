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

package no.rutebanken.baba.organisation.rest.exception;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionSystemException;

class SpringExceptionMapperTest {

  @Test
  void testMapValidationExceptionToBadRequest() {
    Response rsp = new SpringExceptionMapper()
      .toResponse(new TransactionSystemException("", new ValidationException()));
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rsp.getStatus());
  }

  @Test
  void testMapUnknownExceptionToInternalServerError() {
    Response rsp = new SpringExceptionMapper()
      .toResponse(new TransactionSystemException("", new RuntimeException()));
    Assertions.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), rsp.getStatus());
  }
}
