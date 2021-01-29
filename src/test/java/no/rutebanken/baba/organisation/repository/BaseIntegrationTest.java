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

import no.rutebanken.baba.BabaTestApp;
import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.model.organisation.Authority;
import no.rutebanken.baba.organisation.model.organisation.Organisation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = BabaTestApp.class)
@Transactional
public abstract class BaseIntegrationTest {


	@TestConfiguration
	@EnableWebSecurity
	static class TestWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable()
					.authorizeRequests(authorizeRequests ->
							authorizeRequests
									.anyRequest().permitAll()
					);

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
