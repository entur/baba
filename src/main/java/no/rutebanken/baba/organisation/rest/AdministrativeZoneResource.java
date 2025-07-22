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

package no.rutebanken.baba.organisation.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.PathParam;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.baba.organisation.model.organisation.AdministrativeZoneType;
import no.rutebanken.baba.organisation.repository.AdministrativeZoneRepository;
import no.rutebanken.baba.organisation.repository.VersionedEntityRepository;
import no.rutebanken.baba.organisation.rest.dto.organisation.AdministrativeZoneDTO;
import no.rutebanken.baba.organisation.rest.mapper.AdministrativeZoneMapper;
import no.rutebanken.baba.organisation.rest.mapper.DTOMapper;
import no.rutebanken.baba.organisation.rest.validation.AdministrativeZoneValidator;
import no.rutebanken.baba.organisation.rest.validation.DTOValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;

@Component
@Path("administrative_zones")
@Produces("application/json")
@Transactional
@PreAuthorize("@authorizationService.isOrganisationAdmin()")
@Tags(value = {
        @Tag(name = "AdministrativeZoneResource", description ="Administrative zone resource")
})
public class AdministrativeZoneResource extends AnnotatedBaseResource<AdministrativeZone, AdministrativeZoneDTO> {


    private final AdministrativeZoneRepository repository;
    private final AdministrativeZoneMapper mapper;
    private final AdministrativeZoneValidator validator;

    public AdministrativeZoneResource(AdministrativeZoneRepository repository, AdministrativeZoneMapper mapper, AdministrativeZoneValidator validator) {
        this.repository = repository;
        this.mapper = mapper;
        this.validator = validator;
    }


    @Override
    @GET
    @Path("{id}")
    @PreAuthorize("@authorizationService.canViewAllOrganisationData()")
    public AdministrativeZoneDTO get(@PathParam("id") String id) {
        return super.get(id);
    }

    @Override
    @GET
    @PreAuthorize("@authorizationService.canViewAllOrganisationData()")
    public List<AdministrativeZoneDTO> listAll() {
        return super.listAll();
    }



    @Override
    protected VersionedEntityRepository<AdministrativeZone> getRepository() {
        return repository;
    }

    @Override
    protected DTOMapper<AdministrativeZone, AdministrativeZoneDTO> getMapper() {
        return mapper;
    }

    @Override
    protected Class<AdministrativeZone> getEntityClass() {
        return AdministrativeZone.class;
    }

    @Override
    protected DTOValidator<AdministrativeZone, AdministrativeZoneDTO> getValidator() {
        return validator;
    }


    @GET
    @Path("types")
    public AdministrativeZoneType[] getAdministrativeZoneTypes() {
        return AdministrativeZoneType.values();
    }

}
