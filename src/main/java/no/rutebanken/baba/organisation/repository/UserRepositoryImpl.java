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

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;

    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> findUsersWithResponsibilitySet(ResponsibilitySet responsibilitySet) {

        TypedQuery<User> query = entityManager.createQuery("select u from User u where :respSet member of u.responsibilitySets", User.class);

        query.setParameter("respSet", responsibilitySet);

        return query.getResultList();
    }

    @Override
    public User getUserByEmail(String email) {

        TypedQuery<User> query = entityManager.createQuery("select u from User u where u.contactDetails.email = :email", User.class);

        query.setParameter("email", email);

        List<User> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return null;
        }
        if(resultList.size() > 1) {
            throw new IllegalStateException("More than one user found for email " + email);
        }
        return resultList.getFirst();
    }


}
