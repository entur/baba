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

package no.rutebanken.baba.organisation.repository;

import java.util.List;
import no.rutebanken.baba.BabaTestApp;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import no.rutebanken.baba.security.permissionstore.CodespaceMapping;
import no.rutebanken.baba.security.permissionstore.OrganisationRegisterClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = BabaTestApp.class
)
@Transactional
public abstract class BaseIntegrationTest {

  @TestConfiguration
  @EnableWebSecurity
  static class TestWebSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
      return http.build();
    }

    @Bean
    public PermissionStoreClient permissionStoreClient() {
      return new PermissionStoreClient() {
        @Override
        public PermissionStoreUser getUser(String subject) {
          return null;
        }

        @Override
        public boolean isFederated(String domain) {
          return false;
        }
      };
    }

    @Bean
    public OrganisationRegisterClient organisationRegisterClient() {
      return new OrganisationRegisterClient() {
        @Override
        public List<CodespaceMapping> getCodespaceMappings() {
          return List.of();
        }
      };
    }
  }

  @Autowired
  protected CodeSpaceRepository codeSpaceRepository;

  @Autowired
  protected OrganisationRepository organisationRepository;

  protected Organisation defaultOrganisation;

  protected CodeSpace defaultCodeSpace;

  @BeforeEach
  void setUp() {
    CodeSpace codeSpace = new CodeSpace("nsr", "NSR", "http://www.rutebanken.org/ns/nsr");
    defaultCodeSpace = codeSpaceRepository.saveAndFlush(codeSpace);

    Authority authority = new Authority();
    authority.setCodeSpace(defaultCodeSpace);
    authority.setName("Test Org");
    authority.setPrivateCode("testOrg");
    defaultOrganisation = organisationRepository.saveAndFlush(authority);
  }
}
