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

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import no.rutebanken.baba.filter.CorsResponseFilter;
import no.rutebanken.baba.organisation.rest.*;
import no.rutebanken.baba.organisation.rest.exception.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class JerseyConfig {

  @Bean
  public ServletRegistrationBean<ServletContainer> organisationsAPIJerseyConfig(
    @Lazy CodeSpaceResource codeSpaceResource,
    @Lazy OrganisationResource organisationResource,
    @Lazy AdministrativeZoneResource administrativeZoneResource,
    @Lazy UserResource userResource,
    @Lazy M2MClientResource m2MClientResource,
    @Lazy NotificationConfigurationResource notificationConfigurationResource,
    @Lazy RoleResource roleResource,
    @Lazy EntityTypeResource entityTypeResource,
    @Lazy EntityClassificationResource entityClassificationResource,
    @Lazy ResponsibilitySetResource responsibilitySetResource
  ) {
    ServletRegistrationBean<ServletContainer> publicJersey = new ServletRegistrationBean<>(
      new ServletContainer(
        new OrganisationsAPIConfig(
          codeSpaceResource,
          organisationResource,
          administrativeZoneResource,
          userResource,
          m2MClientResource,
          notificationConfigurationResource,
          roleResource,
          entityTypeResource,
          entityClassificationResource,
          responsibilitySetResource
        )
      )
    );
    publicJersey.addUrlMappings("/services/organisations/*");
    publicJersey.setName("OrganisationAPI");
    publicJersey.setLoadOnStartup(0);
    publicJersey.getInitParameters().put("swagger.scanner.id", "organisations-scanner");
    publicJersey.getInitParameters().put("swagger.config.id", "organisations-swagger-doc");
    return publicJersey;
  }

  private static class OrganisationsAPIConfig extends ResourceConfig {

    public OrganisationsAPIConfig(
      CodeSpaceResource codeSpaceResource,
      OrganisationResource organisationResource,
      AdministrativeZoneResource administrativeZoneResource,
      UserResource userResource,
      M2MClientResource m2MClientResource,
      NotificationConfigurationResource notificationConfigurationResource,
      RoleResource roleResource,
      EntityTypeResource entityTypeResource,
      EntityClassificationResource entityClassificationResource,
      ResponsibilitySetResource responsibilitySetResource
    ) {
      register(CorsResponseFilter.class);

      // Register the Spring-managed bean instances (not the classes) so they are fully
      // initialised by Spring (incl. @PreAuthorize proxying). The Jersey/Spring bridge in
      // Boot 4.0 no longer runs @PostConstruct / @PreAuthorize on resources registered by class.
      register(codeSpaceResource);
      register(organisationResource);
      register(administrativeZoneResource);
      register(userResource);
      register(m2MClientResource);
      register(notificationConfigurationResource);
      register(roleResource);
      register(entityTypeResource);
      register(entityClassificationResource);
      register(responsibilitySetResource);

      register(NotAuthenticatedExceptionMapper.class);
      register(PersistenceExceptionMapper.class);
      register(SpringExceptionMapper.class);
      register(IllegalArgumentExceptionMapper.class);
      register(AccessDeniedExceptionMapper.class);
      register(OrganisationExceptionMapper.class);

      register(OpenApiResource.class);
    }
  }
}
