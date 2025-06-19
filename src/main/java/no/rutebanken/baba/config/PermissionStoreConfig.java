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

package no.rutebanken.baba.config;

import no.rutebanken.baba.security.permissionstore.DefaultOrganisationRegisterClient;
import no.rutebanken.baba.security.permissionstore.EnturPartnerM2MRoleAssignmentRepository;
import no.rutebanken.baba.security.permissionstore.DefaultPermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.OrganisationRegisterClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import org.entur.oauth2.AuthorizedWebClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Configure Spring Beans for OAuth2 resource server and OAuth2 client security.
 */
@Configuration
public class PermissionStoreConfig {


  @Bean
  EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRepository(@Value("#{${baba.oauth2.resourceserver.auth0.partner.organisations:{}}}") Map<Long, String> rutebankenOrganisations,
                                                                    @Value("#{${baba.netex.export.block.authorization:{}}}") Map<String, String> authorizedProvidersForNetexBlocksConsumer,
                                                                    @Value("#{${baba.netex.import.delegation.authorization:{}}}") Map<String, String> delegatedNetexDataProviders,
                                                                    @Value("${baba.oauth2.resourceserver.auth0.partner.admin.activated:false}") boolean administratorAccessActivated) {
    return new EnturPartnerM2MRoleAssignmentRepository(rutebankenOrganisations, authorizedProvidersForNetexBlocksConsumer, delegatedNetexDataProviders, administratorAccessActivated);
  }

  @Bean
  @Profile("!test")
  PermissionStoreClient permissionStoreClient(
    @Qualifier("internalWebClient") WebClient internalWebClient,
    @Value("${baba.permissionstore.url}") String permissionStoreUrl
  ) {
    WebClient permissionStoreWebClient = internalWebClient.mutate()
            .baseUrl(permissionStoreUrl)
            .build();
    return new DefaultPermissionStoreClient(permissionStoreWebClient);
  }

  @Bean
  @Profile("!test")
  OrganisationRegisterClient organisationRegisterClient(
          @Qualifier("orgRegisterWebClient") WebClient internalWebClient,
          @Value("${baba.organisation.register.url}") String organisationRegisterUrl
  ) {
    WebClient organisationRegisterWebClient = internalWebClient.mutate()
            .baseUrl(organisationRegisterUrl)
            .build();
    return new DefaultOrganisationRegisterClient(organisationRegisterWebClient);
  }


  @Bean("internalWebClient")
  @Profile("!test")
  WebClient internalWebClient(
    WebClient.Builder webClientBuilder,
    OAuth2ClientProperties properties,
    @Value("${baba.permissionstore.oauth2.client.audience}") String audience,
    ClientHttpConnector clientHttpConnector
  ) {
    return new AuthorizedWebClientBuilder(webClientBuilder)
      .withOAuth2ClientProperties(properties)
      .withAudience(audience)
      .withClientRegistrationId("internal")
      .build()
      .mutate()
      .clientConnector(clientHttpConnector)
      .defaultHeader("Et-Client-Name", "entur-baba")
      .build();
  }

  @Bean("orgRegisterWebClient")
  @Profile("!test")
  WebClient orgRegisterWebClient(
          WebClient.Builder webClientBuilder,
          OAuth2ClientProperties properties,
          @Value("${baba.permissionstore.oauth2.client.audience}") String audience,

          ClientHttpConnector clientHttpConnector
  ) {
    return new AuthorizedWebClientBuilder(webClientBuilder)
            .withOAuth2ClientProperties(properties)
            .withAudience(audience)
            .withClientRegistrationId("orgregister")
            .build()
            .mutate()
            .clientConnector(clientHttpConnector)
            .defaultHeader("Et-Client-Name", "entur-baba")
            .build();
  }
}
