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

package no.rutebanken.baba;

import no.rutebanken.baba.organisation.model.CodeSpace;
import no.rutebanken.baba.organisation.repository.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude={UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {"no.rutebanken.baba.organisation.repository"},
        repositoryBaseClass = BaseRepositoryImpl.class)
@EntityScan(basePackageClasses = {CodeSpace.class, Jsr310JpaConverters.class})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
