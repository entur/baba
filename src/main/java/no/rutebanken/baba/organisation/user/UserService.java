package no.rutebanken.baba.organisation.user;

import jakarta.ws.rs.NotFoundException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import no.rutebanken.baba.security.permissionstore.EnturPartnerM2MRoleAssignmentRepository;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final PermissionStoreClient permissionStoreClient;
    private final EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository;

    public UserService(UserRepository repository, PermissionStoreClient permissionStoreClient, EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository) {
        this.repository = repository;
        this.permissionStoreClient = permissionStoreClient;
        this.enturPartnerM2MRoleAssignmentRepository = enturPartnerM2MRoleAssignmentRepository;
    }

    /**
     * Return a user from the database, identified by username.
     * @deprecated use {@link #getUserByAuthenticatedUser(AuthenticatedUser)}
     */
    @Deprecated
    public User getUserByUsername(String username) {
        User user = repository.getUserByUsername(username);
        if (user == null) {
            throw new NotFoundException("User with user name: [" + username + "] not found");
        }
        return user;
    }

    /**
     * Return a user from the Baba database, identified by an OAuth2 authenticated user.
     * Machine-to-machine clients are not supported.
     * @throws IllegalArgumentException if the authenticated user is a machine-to-machine client.
     * @throws NotFoundException if the authenticated user cannot be found in the Baba database.
     */
    public User getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if(authenticatedUser.isClient()) {
            throw new IllegalArgumentException("machine-to-machine tokens are not supported for getUserByAuthenticatedUser: " + authenticatedUser);
        } else if(authenticatedUser.isRor()) {
            return repository.getUserByUsername(authenticatedUser.username());
        }
        else {
            PermissionStoreUser permissionStoreUser = permissionStoreClient.getUser(authenticatedUser.subject());
            User user = repository.getUserByEmail(permissionStoreUser.email);
            if (user == null) {
                throw new NotFoundException("User with user name: [" + authenticatedUser + "] not found");
            }
            return user;
        }
    }

    /**
     * Return the role assignments for a user in the database, identified by userName.
     * @deprecated use {@link #roleAssignments(AuthenticatedUser)}
     */
    @Deprecated
    public List<RoleAssignment> roleAssignments(String username) {
        User user = getUserByUsername(username);
         return toRoleAssignments(user);
    }

    /**
     * Return the role assignments for an OAuth2 subject.
     * Role assignments for actual users are extracted from the Baba database.
     * Role assignments for Entur Partner machine-to-machine tokens are built from configuration.
     * Role assignments for Entur Internal machine-to-machine tokens are not processed here. The role assignments can be
     * built directly from the permissions claim in the token.
     */
    public List<RoleAssignment> roleAssignments(AuthenticatedUser authenticatedUser) {
        if(authenticatedUser.isClient()) {
            if(!authenticatedUser.isPartner()) {
                throw new IllegalArgumentException("machine-to-machine tokens are not supported for this authority: " + authenticatedUser);
            }
            return enturPartnerM2MRoleAssignmentRepository.getRolesAssignments(authenticatedUser.organisationId());
        } else if(authenticatedUser.isRor()) {
            User user = repository.getUserByUsername(authenticatedUser.username());
            if (user == null) {
                throw new NotFoundException("User with user name: [" + authenticatedUser.subject() + "] not found");
            }
            return toRoleAssignments(user);
        }
        else {
            PermissionStoreUser permissionStoreUser = permissionStoreClient.getUser(authenticatedUser.subject());
            User user = repository.getUserByEmail(permissionStoreUser.email);
            if (user == null) {
                throw new NotFoundException("User with user name: [" + authenticatedUser.subject() + "] not found");
            }
            return toRoleAssignments(user);
        }
    }

    private List<RoleAssignment> toRoleAssignments(User user) {
        List<RoleAssignment> list = user.getResponsibilitySets().stream()
                .map(ResponsibilitySet::getRoles)
                .flatMap(Collection::stream)
                .map(RoleAssignmentMapper::toRoleAssignment)
                .toList();
        return list;
    }

}
