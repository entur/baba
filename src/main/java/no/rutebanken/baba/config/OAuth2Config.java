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

import java.util.Arrays;
import org.entur.oauth2.multiissuer.MultiIssuerAuthenticationManagerResolver;
import org.entur.oauth2.multiissuer.MultiIssuerAuthenticationManagerResolverBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configure Spring Beans for OAuth2 resource server and OAuth2 client security.
 */
@Configuration
public class OAuth2Config {

  @Bean
  @Profile("!test")
  public MultiIssuerAuthenticationManagerResolver multiIssuerAuthenticationManagerResolver(
    @Value(
      "${baba.oauth2.resourceserver.auth0.entur.internal.jwt.audience:}"
    ) String enturInternalAuth0Audience,
    @Value(
      "${baba.oauth2.resourceserver.auth0.entur.internal.jwt.issuer-uri:}"
    ) String enturInternalAuth0Issuer,
    @Value(
      "${baba.oauth2.resourceserver.auth0.entur.partner.jwt.audience:}"
    ) String enturPartnerAuth0Audience,
    @Value(
      "${baba.oauth2.resourceserver.auth0.entur.partner.jwt.issuer-uri:}"
    ) String enturPartnerAuth0Issuer,
    @Value("${baba.oauth2.resourceserver.auth0.ror.jwt.audience:}") String rorAuth0Audience
  ) {
    return new MultiIssuerAuthenticationManagerResolverBuilder()
      .withEnturInternalAuth0Issuer(enturInternalAuth0Issuer)
      .withEnturInternalAuth0Audiences(
        Arrays.stream(enturInternalAuth0Audience.split(",")).toList()
      )
      .withEnturPartnerAuth0Issuer(enturPartnerAuth0Issuer)
      .withEnturPartnerAuth0Audiences(Arrays.stream(enturPartnerAuth0Audience.split(",")).toList())
      .build();
  }
}
