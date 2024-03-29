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

package no.rutebanken.baba.security;


import no.rutebanken.baba.provider.domain.Provider;
import no.rutebanken.baba.provider.repository.ProviderRepository;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_VIEW_ALL;

@Service
public class ProviderAuthenticationService {


    /**
     * Default role prefix added by Spring Security.
     *
     * @see SecurityExpressionRoot
     */
    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

    private static final String PREFIXED_ROLE_ROUTE_DATA_VIEW_ALL = DEFAULT_ROLE_PREFIX + ROLE_ROUTE_DATA_VIEW_ALL;
    private static final String PREFIXED_ROLE_ROUTE_DATA_ADMIN = DEFAULT_ROLE_PREFIX + ROLE_ROUTE_DATA_ADMIN;


    private final ProviderRepository providerRepository;
    private final RoleAssignmentExtractor roleAssignmentExtractor;
    private final boolean authorizationEnabled;

    public ProviderAuthenticationService(ProviderRepository providerRepository, RoleAssignmentExtractor roleAssignmentExtractor, @Value("${authorization.enabled:true}") boolean authorizationEnabled) {
        this.providerRepository = providerRepository;
        this.roleAssignmentExtractor = roleAssignmentExtractor;
        this.authorizationEnabled = authorizationEnabled;
    }


    public boolean hasRoleForProvider(Authentication authentication, String role, Long providerId) {
        if (!authorizationEnabled) {
            return true;
        }
        if (providerId == null) {
            return false;
        }
        Provider provider = providerRepository.getProvider(providerId);
        if (provider == null) {
            return false;
        }

        return roleAssignmentExtractor.getRoleAssignmentsForUser(authentication).stream()
                .filter(ra -> role.equals(ra.r)).anyMatch(ra -> provider.chouetteInfo.referential.toUpperCase().equals(ra.o));
    }


    public boolean canViewAllProviders(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
                grantedAuthority.getAuthority().equals(PREFIXED_ROLE_ROUTE_DATA_ADMIN) || grantedAuthority.getAuthority().equals(PREFIXED_ROLE_ROUTE_DATA_VIEW_ALL));
    }
}
