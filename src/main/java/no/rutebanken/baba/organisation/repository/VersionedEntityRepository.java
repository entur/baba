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

import java.util.List;
import no.rutebanken.baba.organisation.model.VersionedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionedEntityRepository<T extends VersionedEntity>
  extends JpaRepository<T, Long> {
  /** Get one, or throw exception if no entity with id exists */
  T getOneByPublicId(String id);

  /** Get one, or null if no entity with id exists */
  T getOneByPublicIdIfExists(String id);

  @Override
  List<T> findAll();
}
