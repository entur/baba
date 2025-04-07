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

import no.rutebanken.baba.organisation.user.UserService;
import com.auth0.client.auth.AuthAPI;
import no.rutebanken.baba.organisation.repository.RoleRepository;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.service.Auth0IamService;
import no.rutebanken.baba.organisation.service.IamService;
import no.rutebanken.baba.organisation.service.NoopIamService;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.entur.oauth2.user.JwtUserInfoExtractor;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.helper.organisation.authorization.AuthorizationService;
import org.rutebanken.helper.organisation.authorization.DefaultAuthorizationService;
import org.rutebanken.helper.organisation.authorization.FullAccessAuthorizationService;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Configure authorization.
 */
@Configuration
public class AuthorizationConfig {

    @ConditionalOnProperty(
            value = "baba.security.role.assignment.extractor",
            havingValue = "jwt",
            matchIfMissing = true
    )
    @Bean
    public RoleAssignmentExtractor jwtRoleAssignmentExtractor() {
        return new JwtRoleAssignmentExtractor();
    }

    @ConditionalOnProperty(
            value = "baba.security.role.assignment.extractor",
            havingValue = "baba"
    )
    @Bean
    public RoleAssignmentExtractor babaRoleAssignmentExtractor(UserService userService) {
        return new LocalBabaRoleAssignmentExtractor(userService);
    }

    @Bean
    public UserInfoExtractor userInfoExtractor() {
        return new JwtUserInfoExtractor();
    }

    @ConditionalOnProperty(
            value = "baba.security.authorization-service",
            havingValue = "token-based"
    )
    @Bean("authorizationService")
    public AuthorizationService<Long> tokenBasedAuthorizationService(RoleAssignmentExtractor roleAssignmentExtractor) {
        return new DefaultAuthorizationService<>(roleAssignmentExtractor);
    }

    @ConditionalOnProperty(
            value = "baba.security.authorization-service",
            havingValue = "full-access"
    )
    @Bean("authorizationService")
    public AuthorizationService<Long> fullAccessAuthorizationService() {
        return new FullAccessAuthorizationService();
    }


    @Profile("auth0")
    @Bean("iamService")
    public IamService auth0IamService(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      AuthAPI authAPI,
                                      @Value("#{'${iam.auth0.default.roles:rutebanken}'.split(',')}") List<String> defaultRoles,
                                      @Value("${iam.auth0.admin.domain}") String domain){
        return new Auth0IamService(userRepository, roleRepository, authAPI, defaultRoles, domain);
    }

    @ConditionalOnMissingBean(
            value = IamService.class,
            ignored = NoopIamService.class
    )
    @Bean("iamService")
    public IamService noopIamService(){
        return new NoopIamService();
    }

}


