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

@Configuration
public class JerseyConfig {

  @Bean
  public ServletRegistrationBean<ServletContainer> organisationsAPIJerseyConfig() {
    ServletRegistrationBean<ServletContainer> publicJersey = new ServletRegistrationBean<>(
      new ServletContainer(new OrganisationsAPIConfig())
    );
    publicJersey.addUrlMappings("/services/organisations/*");
    publicJersey.setName("OrganisationAPI");
    publicJersey.setLoadOnStartup(0);
    publicJersey.getInitParameters().put("swagger.scanner.id", "organisations-scanner");
    publicJersey.getInitParameters().put("swagger.config.id", "organisations-swagger-doc");
    return publicJersey;
  }

  private static class OrganisationsAPIConfig extends ResourceConfig {

    public OrganisationsAPIConfig() {
      register(CorsResponseFilter.class);

      // Register the resource CLASSES (not bean instances). The resources are constructor-injected
      // Spring @Components, so Jersey's SpringComponentProvider resolves each one to its single
      // matching bean - the @PreAuthorize CGLIB proxy - which keeps method-level authorization
      // working. Registering the proxy *instances* instead would make Jersey introspect the
      // generated proxy class, which erases the generic BaseResource<E, D> return types
      // (e.g. listAll(): List<D>) and produces an inaccurate OpenAPI schema plus misleading
      // startup ERROR/WARN noise.
      register(CodeSpaceResource.class);
      register(OrganisationResource.class);
      register(AdministrativeZoneResource.class);
      register(UserResource.class);
      register(M2MClientResource.class);
      register(NotificationConfigurationResource.class);
      register(RoleResource.class);
      register(EntityTypeResource.class);
      register(EntityClassificationResource.class);
      register(ResponsibilitySetResource.class);

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
