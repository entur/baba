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

package no.rutebanken.baba.organisation.repository;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import org.springframework.stereotype.Repository;

@Repository
public class ResponsibilitySetRepositoryImpl implements ResponsibilitySetRepositoryCustom {

  private final EntityManager entityManager;

  public ResponsibilitySetRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<ResponsibilitySet> getResponsibilitySetsReferringTo(VersionedEntity entity) {
    if (entity instanceof Role) {
      return entityManager
        .createQuery(
          "select rs from ResponsibilitySet rs inner join rs.roles r" +
          " where r.typeOfResponsibilityRole=:role",
          ResponsibilitySet.class
        )
        .setParameter("role", entity)
        .getResultList();
    }

    // TODO hanle other entity types
    return new ArrayList<>();
  }
}
