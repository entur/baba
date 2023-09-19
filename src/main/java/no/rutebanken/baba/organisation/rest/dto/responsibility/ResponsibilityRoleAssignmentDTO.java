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

package no.rutebanken.baba.organisation.rest.dto.responsibility;

import io.swagger.v3.oas.annotations.media.Schema;
import no.rutebanken.baba.organisation.rest.dto.BaseDTO;

import java.util.ArrayList;
import java.util.List;
@Schema(description = "Reusable authorization grant. Combination of a role with one or more of: organisations, admin zone and list of entity classification assignments. ")
public class ResponsibilityRoleAssignmentDTO extends BaseDTO {

	public String typeOfResponsibilityRoleRef;
	public String responsibleOrganisationRef;
	public String responsibleAreaRef;

	public List<EntityClassificationAssignmentDTO> entityClassificationAssignments = new ArrayList<>();

	public ResponsibilityRoleAssignmentDTO() {
	}

	public ResponsibilityRoleAssignmentDTO(String typeOfResponsibilityRoleRef, String responsibleOrganisationRef) {
		this.typeOfResponsibilityRoleRef = typeOfResponsibilityRoleRef;
		this.responsibleOrganisationRef = responsibleOrganisationRef;
	}
}
