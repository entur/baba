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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = "code_space_unique_private_code",
      columnNames = { "privateCode", "entityVersion" }
    ),
    @UniqueConstraint(name = "code_space_unique_xmlns", columnNames = { "xmlns", "entityVersion" }),
  }
)
public class CodeSpace extends VersionedEntity {

  @NotNull
  @Column(unique = true)
  private String xmlns;

  @NotNull
  @Column(unique = true)
  private String xmlnsUrl;

  public CodeSpace(String typeId, String xmlns, String xmlnsUrl) {
    this.setPrivateCode(typeId);
    this.xmlns = xmlns;
    this.xmlnsUrl = xmlnsUrl;
  }

  public CodeSpace() {}

  public String getXmlns() {
    return xmlns;
  }

  public void setXmlns(String xmlns) {
    this.xmlns = xmlns;
  }

  public String getXmlnsUrl() {
    return xmlnsUrl;
  }

  public void setXmlnsUrl(String xmlnsUrl) {
    this.xmlnsUrl = xmlnsUrl;
  }
}
