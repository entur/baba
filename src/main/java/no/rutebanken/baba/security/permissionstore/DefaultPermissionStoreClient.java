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
 */

package no.rutebanken.baba.security.permissionstore;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Client for accessing the Permission Store API.
 */
public class DefaultPermissionStoreClient implements PermissionStoreClient {

  private static final long MAX_RETRY_ATTEMPTS = 3;

  private final WebClient webClient;

  public DefaultPermissionStoreClient(WebClient permissionStoreWebClient) {
    this.webClient = permissionStoreWebClient;
  }

  @Override
  public  PermissionStoreUser getUser(String subject) {
    List<PermissionStoreUser> users = webClient
            .get()
            .uri(uriBuilder ->
                    uriBuilder
                            .path("/users")
                            .queryParam("subject", subject)
                            .build()
            )
            .retrieve()
            .bodyToFlux(PermissionStoreUser.class)
            .collectList()
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1)).filter(is5xx))
            .block();
    if (users == null || users.isEmpty()) {
      throw new IllegalArgumentException("No users found for subject " + subject);
    }
    if(users.size() > 1) {
      throw new IllegalStateException("Multiple users found for subject " + subject);
    }
    return users.getFirst();
  }

  private static final Predicate<Throwable> is5xx = throwable ->
    throwable instanceof WebClientResponseException webClientResponseException &&
    webClientResponseException.getStatusCode().is5xxServerError();
}
