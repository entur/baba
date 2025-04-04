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
     * Return the role assignments for a user in the database, identified by userName or email.
     */
    public List<RoleAssignment> roleAssignments(String userName) {
        User user;
        // A token coming from the Entur Partner Tenant maps the preferred user name to the user email.
        // A token coming from the RoR Partner Tenant maps the preferred user name to the Baba user name.
        // The character "@" is forbidden in Baba user names.
        if (userName.contains("@")) {
            user = repository.getUserByEmail(userName);
        } else {
            user = repository.getUserByUsername(userName);
        }

        if (user == null) {
            throw new NotFoundException("User with user name: [" + userName + "] not found");
        }

        List<RoleAssignment> list = user.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment)
                .toList();
        return list;
    }

}
