package no.rutebanken.baba.organisation.user;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.repository.UserRepository;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import no.rutebanken.baba.security.permissionstore.EnturInternalM2MRoleAssignmentRepository;
import no.rutebanken.baba.security.permissionstore.EnturPartnerM2MRoleAssignmentRepository;
import no.rutebanken.baba.security.permissionstore.PermissionStoreClient;
import no.rutebanken.baba.security.permissionstore.PermissionStoreUser;
import org.entur.ror.permission.AuthenticatedUser;
import org.entur.ror.permission.BabaContactDetails;
import org.entur.ror.permission.BabaUser;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PermissionStoreClient permissionStoreClient;
    private final EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository;
    private final EnturInternalM2MRoleAssignmentRepository enturInternalM2MRoleAssignmentRepository;

    public UserService(UserRepository repository, PermissionStoreClient permissionStoreClient, EnturPartnerM2MRoleAssignmentRepository enturPartnerM2MRoleAssignmentRepository, EnturInternalM2MRoleAssignmentRepository enturInternalM2MRoleAssignmentRepository) {
        this.repository = repository;
        this.permissionStoreClient = permissionStoreClient;
        this.enturPartnerM2MRoleAssignmentRepository = enturPartnerM2MRoleAssignmentRepository;
        this.enturInternalM2MRoleAssignmentRepository = enturInternalM2MRoleAssignmentRepository;
    }

    /**
     * Return details about an authenticated user.
     * The user can either represent a user account from the Baba database or a machine-to-machine client.
     * @throws NotFoundException if the authenticated user cannot be found.
     */
    public BabaUser getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        if(authenticatedUser.isClient()) {
            BabaUser babaUser = new BabaUser();
            babaUser.isClient = true;
            if(authenticatedUser.isPartner()) {
                babaUser.username =enturPartnerM2MRoleAssignmentRepository.getRutebankenOrganisationId(authenticatedUser.organisationId());
            } else {
                babaUser.username = "Entur Internal/" + authenticatedUser.subject();
            }
            return babaUser;
        } else if(authenticatedUser.isRor()) {
            User user = repository.getUserByUsername(authenticatedUser.username());
            return mapBabaUser(user);
        }
        else {
            User user = permissionStoreUser(authenticatedUser);
            return mapBabaUser(user);
        }
    }

    private static BabaUser mapBabaUser(User user) {
        BabaUser babaUser = new BabaUser();
        babaUser.username = user.getUsername();
        babaUser.contactDetails = new BabaContactDetails();
        babaUser.contactDetails.firstName = user.getContactDetails().getFirstName();
        babaUser.contactDetails.lastName = user.getContactDetails().getLastName();
        babaUser.contactDetails.email = user.getContactDetails().getEmail();
        return babaUser;
    }

    /**
     * Return the role assignments for an OAuth2 subject.
     * Role assignments for actual users are extracted from the Baba database.
     * Role assignments for Entur Partner machine-to-machine tokens are built from configuration.
     * Role assignments for Entur Internal machine-to-machine tokens are mapped directly from the permissions listed in the token.
     */
    public List<RoleAssignment> roleAssignments(AuthenticatedUser authenticatedUser) {
        if(authenticatedUser.isClient()) {
            if(authenticatedUser.isInternal()) {
                return  enturInternalM2MRoleAssignmentRepository.getRolesAssignments(authenticatedUser);
            } else if(authenticatedUser.isPartner()) {
                return enturPartnerM2MRoleAssignmentRepository.getRolesAssignments(authenticatedUser.organisationId());
            } else {
                throw new IllegalArgumentException("Unknown client " + authenticatedUser);
            }
        } else if(authenticatedUser.isRor()) {
            User user = repository.getUserByUsername(authenticatedUser.username());
            if (user == null) {
                throw new NotFoundException("User with user name: [" + authenticatedUser.subject() + "] not found");
            }
            return toRoleAssignments(user);
        }
        else {
            User user = permissionStoreUser(authenticatedUser);
            return toRoleAssignments(user);
        }
    }

    private User permissionStoreUser(AuthenticatedUser authenticatedUser) {
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
        LOGGER.debug("Retrieving user with email '{}' in Baba database", normalizedEmail);
        User user = repository.getUserByEmail(normalizedEmail);
        if (user == null) {
            LOGGER.debug("No user found in Baba database with email '{}' : permission store user = {}", normalizedEmail, permissionStoreUser);
            throw new NotFoundException("User with subject: [" + authenticatedUser + "] not found");
        }
        LOGGER.debug("Found user with email '{}' in Baba database: '{}'", normalizedEmail, user.getUsername());
        return user;
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
