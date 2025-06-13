package no.rutebanken.baba.organisation.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;
import no.rutebanken.baba.exceptions.BabaException;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.service.IamService;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import no.rutebanken.baba.security.permissionstore.EnturPartnerM2MRoleAssignmentRepository;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PermissionStoreClient permissionStoreClient;
    private final EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository;
    private final IamService iamService;

    public UserService(UserRepository repository, PermissionStoreClient permissionStoreClient, EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository, IamService iamService) {
        this.repository = repository;
        this.permissionStoreClient = permissionStoreClient;
        this.enturPartnerM2MRoleAssignmentRepository = enturPartnerM2MRoleAssignmentRepository;
        this.iamService = iamService;
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
            LOGGER.debug("Retrieving user {} in Entur Partner", authenticatedUser.subject());
            PermissionStoreUser permissionStoreUser = permissionStoreClient.getUser(authenticatedUser.subject());
            if(permissionStoreUser == null) {
                LOGGER.debug("User not found in Entur Partner: {}", authenticatedUser.subject());
                throw new NotFoundException("User with subject '" + authenticatedUser.subject() + "' not found in Entur Partner");
            }
            LOGGER.debug("Found Entur Partner user for subject {} : {}", authenticatedUser.subject(), permissionStoreUser);
            if(permissionStoreUser.email == null) {
                LOGGER.debug("User without email in Entur Partner: {}", authenticatedUser.subject());
                throw new ServerErrorException("User with subject '" + authenticatedUser.subject() + "' has no email in Entur Partner", Response.Status.INTERNAL_SERVER_ERROR);
            }
            String normalizedEmail = permissionStoreUser.email.toLowerCase();
            User user = repository.getUserByEmail(normalizedEmail);
            if (user == null) {
                LOGGER.debug("No user found in Baba database with email '{}' : permission store user = {}", normalizedEmail, permissionStoreUser);
                throw new NotFoundException("User with subject: [" + authenticatedUser + "] not found");
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

    public String listUsers() {
        return repository.findAll().stream()
                .filter(User::isPersonalAccount)
                .sorted(Comparator.comparing(User::getUsername))
                .map(this::mapUser)
                .collect(Collectors.joining("\n"));

    }

    public String jsonlistUsers() {
        String userExport = toJson(repository.findAll().stream()
                .filter(User::isPersonalAccount)
                .filter(this::shouldExport)
                .sorted(Comparator.comparing(User::getUsername))
                .map(this::mapUserJson)
                .toList());
        System.out.println(userExport);
        return userExport;

    }

    private boolean shouldExport(User user) {
        // prevent rate limiting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String email = user.getContactDetails().getEmail();
        boolean isFederated = permissionStoreClient.isFederated(email);
        boolean existsInEnturPartner = isFederated || iamService.hasUserWithEmail(email);
        return !existsInEnturPartner;
    }

    private String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, object);
            return writer.toString();
        } catch (IOException e) {
            throw new BabaException(e);
        }
    }

    private com.auth0.json.mgmt.users.User mapUserJson(User user) {

        com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
        auth0User.setEmail(user.getContactDetails().getEmail());
        auth0User.setGivenName(user.getContactDetails().getFirstName());
        auth0User.setFamilyName(user.getContactDetails().getLastName());
        auth0User.setNickname(user.getUsername());
        auth0User.setAppMetadata(Map.of("organisationId", -1, "employeeId", ""));
        return auth0User;
    }

    private String mapUser(User user) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String email = user.getContactDetails().getEmail();
        boolean isFederated = permissionStoreClient.isFederated(email);
        boolean existsInEnturPartner = isFederated || iamService.hasUserWithEmail(email);
        String userInfo = user.getUsername() + "," + email + "," + user.getOrganisation().getPrivateCode() + "," + isFederated + "," + existsInEnturPartner;
        System.out.println(userInfo);
        return userInfo;
    }


}
