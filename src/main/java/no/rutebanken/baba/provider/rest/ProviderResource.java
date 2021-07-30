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

package no.rutebanken.baba.provider.rest;

import io.swagger.annotations.Api;
import no.rutebanken.baba.chouette.ChouetteReferentialService;
import no.rutebanken.baba.exceptions.ChouetteServiceException;
import no.rutebanken.baba.exceptions.ReferentialAlreadyExistException;
import no.rutebanken.baba.provider.domain.Provider;
import no.rutebanken.baba.provider.domain.TransportMode;
import no.rutebanken.baba.provider.repository.ProviderRepository;
import no.rutebanken.baba.security.ProviderAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_VIEW_ALL;


@Component
@Produces("application/json")
@Path("")
@Api
@PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "')")
public class ProviderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderResource.class);

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ChouetteReferentialService chouetteReferentialService;

    @Autowired
    private ProviderAuthenticationService providerAuthenticationService;

    @GET
    @Path("/{providerId}")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerId)")
    public Provider getProvider(@PathParam("providerId") Long providerId) {
        LOGGER.debug("Returning provider with id '{}'", providerId);
        Provider provider = providerRepository.getProvider(providerId);
        if (provider == null) {
            throw new NotFoundException("Unable to find provider with id=" + providerId);
        }
        return provider;
    }

    @DELETE
    @Path("/{providerId}")
    public void deleteProvider(@PathParam("providerId") Long providerId) {
        LOGGER.info("Deleting provider with id '{}'", providerId);
        Provider provider = providerRepository.getProvider(providerId);
        if (provider == null) {
            throw new NotFoundException("Unable to find provider with id=" + providerId);
        }
        chouetteReferentialService.deleteChouetteReferential(provider);
        providerRepository.deleteProvider(providerId);
    }

    @PUT
    @Path("/{providerId}")
    public void updateProvider(Provider provider) {
        LOGGER.info("Updating provider {}", provider);
        Long providerId = provider.getId();
        Provider existingProvider = providerRepository.getProvider(providerId);
        if (existingProvider == null) {
            throw new NotFoundException("Unable to find provider with id=" + providerId);
        }
        chouetteReferentialService.updateChouetteReferential(provider);
        providerRepository.updateProvider(provider);
    }

    @POST
    public Provider createProvider(Provider provider) {
        LOGGER.info("Creating provider {}", provider);
        String referential = provider.getChouetteInfo().referential;
        if(providerRepository.getProvider(referential) != null) {
            LOGGER.warn("Failed to create provider {}: the provider already exists in the database", provider);
            throw new ReferentialAlreadyExistException(referential);
        }
        try {
            chouetteReferentialService.createChouetteReferential(provider);
        } catch (ChouetteServiceException e) {
            LOGGER.warn("Failed to create provider {}", provider, e);
            throw new InternalServerErrorException("Chouette Server error while creating the provider with id= " + provider.getId() );
        }
        return providerRepository.createProvider(provider);
    }


    /**
     * Return the list of providers.
     * Route data administrators, editors and viewers can access this method, but they will retrieve only the providers they have access to.
     */
    @GET
    @PreAuthorize("hasAnyRole('" + ROLE_ROUTE_DATA_ADMIN + "," + ROLE_ROUTE_DATA_EDIT + "," + ROLE_ROUTE_DATA_VIEW_ALL + "')")
    public Collection<Provider> getProviders() {
        Collection<Provider> providers = providerRepository.getProviders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(providerAuthenticationService.canViewAllProviders(authentication)) {
            LOGGER.debug("Returning all providers.");
            return providers;
        }
        LOGGER.debug("Returning authorized providers.");
        return providers.stream().filter(provider ->  providerAuthenticationService.hasRoleForProvider(authentication, ROLE_ROUTE_DATA_EDIT, provider.getId())).collect(Collectors.toList());
    }


    @GET
    @Path("transport_modes")
    public TransportMode[] getTransportModes() {
        return TransportMode.values();
    }

}
