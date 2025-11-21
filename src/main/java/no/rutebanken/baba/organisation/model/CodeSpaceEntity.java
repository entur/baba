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

package no.rutebanken.baba.organisation.model;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;

@MappedSuperclass
public abstract class CodeSpaceEntity extends VersionedEntity {

  @NotNull
  @JoinColumn(name = "code_space_pk")
  @ManyToOne
  private CodeSpace codeSpace;

  public CodeSpace getCodeSpace() {
    return codeSpace;
  }

  public void setCodeSpace(CodeSpace codeSpace) {
    this.codeSpace = codeSpace;
  }

  @Override
  public String getId() {
    return String.join(":", codeSpace.getXmlns(), getType(), getPrivateCode());
  }
}
