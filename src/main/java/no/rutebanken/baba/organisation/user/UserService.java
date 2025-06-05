package no.rutebanken.baba.organisation.user;

import jakarta.ws.rs.NotFoundException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }


    /**
     * Return a user from the database, identified by userName or email.
     */
    public User getUserByUsername(String username) {
        User user;
        // A token coming from the Entur Partner Tenant maps the preferred user name to the user email.
        // A token coming from the RoR Partner Tenant maps the preferred user name to the Baba user name.
        // The character "@" is forbidden in Baba user names.
        if (username.contains("@")) {
            user = repository.getUserByEmail(username);
        } else {
            user = repository.getUserByUsername(username);
        }

        if (user == null) {
            throw new NotFoundException("User with user name: [" + username + "] not found");
        }

        return user;
    }

    /**
     * Return the role assignments for a user in the database, identified by userName or email.
     */
    public List<RoleAssignment> roleAssignments(String username) {
        User user = getUserByUsername(username);
        List<RoleAssignment> list = user.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment)
                .toList();
        return list;
    }

}
