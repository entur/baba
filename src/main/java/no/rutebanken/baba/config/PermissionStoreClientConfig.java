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

import no.rutebanken.baba.organisation.service.PermissionStoreRoleUpdater;
import org.entur.auth.permission.client.AuthorizeTenant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configure Spring Beans for OAuth2 resource server and OAuth2 client security.
 */
@Configuration
public class PermissionStoreClientConfig {

  @Bean
  @Profile("!test")
  public PermissionStoreRoleUpdater permissionStoreRoleUpdater(AuthorizeTenant authorizeTenant) {
    return new PermissionStoreRoleUpdater(authorizeTenant);
  }
}
