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

package no.rutebanken.baba.security.permissionstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class DefaultPermissionStoreClientTest {

  private static final String TEST_SUBJECT = "testSubject";

  @Test
  void returnsTheUserFoundForTheSubject() {
    PermissionStoreUser user = client(json("[{\"subject\":\"testSubject\",\"email\":\"a@b.no\"}]"))
      .getUser(TEST_SUBJECT);

    assertEquals(TEST_SUBJECT, user.subject);
    assertEquals("a@b.no", user.email);
  }

  @Test
  void returnsNullWhenNoUserMatchesTheSubject() {
    assertNull(client(json("[]")).getUser(TEST_SUBJECT));
  }

  @ParameterizedTest
  @ValueSource(
    strings = { "samlp|acme|ola+test@acme.no", "auth0|a b&c=d#e%f", "waad|{not-a-variable}" }
  )
  void encodesTheSubjectInTheQueryString(String subject) {
    AtomicReference<URI> requestedUri = new AtomicReference<>();
    WebClient webClient = WebClient
      .builder()
      .baseUrl("http://localhost")
      .exchangeFunction(request -> {
        requestedUri.set(request.url());
        return Mono.just(json("[]"));
      })
      .build();

    new DefaultPermissionStoreClient(webClient).getUser(subject);

    String expectedQuery = "subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8);
    assertEquals(expectedQuery.replace("+", "%20"), requestedUri.get().getRawQuery());
  }

  @Test
  void reportsUnavailableWhenThePermissionStoreAnswersNotFound() {
    DefaultPermissionStoreClient client = client(
      ClientResponse.create(HttpStatus.NOT_FOUND).build()
    );

    assertThrows(PermissionStoreUnavailableException.class, () -> client.getUser(TEST_SUBJECT));
  }

  @Test
  void failsWhenSeveralUsersMatchTheSubject() {
    DefaultPermissionStoreClient client = client(
      json("[{\"subject\":\"testSubject\"},{\"subject\":\"testSubject\"}]")
    );

    assertThrows(IllegalStateException.class, () -> client.getUser(TEST_SUBJECT));
  }

  @Test
  void reportsUnavailableWhenThePermissionStoreRejectsTheRequest() {
    DefaultPermissionStoreClient client = client(
      ClientResponse.create(HttpStatus.FORBIDDEN).build()
    );

    assertThrows(PermissionStoreUnavailableException.class, () -> client.getUser(TEST_SUBJECT));
  }

  @Test
  void reportsUnavailableWhenThePermissionStoreCannotBeReached() {
    WebClient webClient = WebClient
      .builder()
      .baseUrl("http://localhost")
      .exchangeFunction(request -> Mono.error(new ConnectException("Connection refused")))
      .build();
    DefaultPermissionStoreClient client = new DefaultPermissionStoreClient(webClient);

    assertThrows(PermissionStoreUnavailableException.class, () -> client.getUser(TEST_SUBJECT));
  }

  @Test
  void reportsUnavailableWhenTheResponseCannotBeRead() {
    DefaultPermissionStoreClient client = client(json("not json"));

    assertThrows(PermissionStoreUnavailableException.class, () -> client.getUser(TEST_SUBJECT));
  }

  private static ClientResponse json(String body) {
    return ClientResponse
      .create(HttpStatus.OK)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .body(body)
      .build();
  }

  private static DefaultPermissionStoreClient client(ClientResponse response) {
    WebClient webClient = WebClient
      .builder()
      .baseUrl("http://localhost")
      .exchangeFunction(request -> Mono.just(response))
      .build();
    return new DefaultPermissionStoreClient(webClient);
  }
}
