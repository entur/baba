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
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import no.rutebanken.baba.organisation.model.CodeSpaceEntity;
import no.rutebanken.baba.organisation.model.Id;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class BaseRepositoryImpl<T extends VersionedEntity>
  extends SimpleJpaRepository<T, Long>
  implements VersionedEntityRepository<T> {

  private final EntityManager entityManager;
  private final JpaEntityInformation<T, Long> entityInformation;

  public BaseRepositoryImpl(
    JpaEntityInformation<T, Long> entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
    this.entityInformation = entityInformation;
  }

  @Override
  public T getOneByPublicIdIfExists(String publicId) {
    Id id = Id.fromString(publicId);

    String jpql =
      "select e from " + entityInformation.getEntityName() + " e  where e.privateCode=:privateCode";
    if (CodeSpaceEntity.class.isAssignableFrom(getDomainClass())) {
      jpql += " and e.codeSpace.xmlns=:codeSpace";
    }

    TypedQuery<T> query = entityManager.createQuery(jpql, getDomainClass());
    query.setParameter("privateCode", id.privateCode());

    if (CodeSpaceEntity.class.isAssignableFrom(getDomainClass())) {
      query.setParameter("codeSpace", id.codeSpace());
    }

    List<T> results = query
      .getResultList()
      .stream()
      .filter(r -> id.type() == null || r.getType().equals(id.type()))
      .toList();

    if (results.size() == 1) {
      return results.getFirst();
    } else if (results.isEmpty()) {
      return null;
    }
    throw new IllegalArgumentException("Query for one entity returned multiple: " + query);
  }

  @Override
  public T getOneByPublicId(String publicId) {
    T entity = getOneByPublicIdIfExists(publicId);
    if (entity == null) {
      throw new EntityNotFoundException(
        entityInformation.getEntityName() + " with id: [" + publicId + "] not found"
      );
    }
    return entity;
  }
}
