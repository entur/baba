package no.rutebanken.baba.config;

import no.rutebanken.baba.organisation.user.UserService;
import org.entur.oauth2.RoROAuth2Claims;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Role assignment extractor used locally in Baba. This implementation retrieves the role assignments directly from the
 * Baba database. Other components use BabaRoleAssignmentExtractor to retrieve the role assignments through the Baba
 * user endpoint.
 */
class LocalBabaRoleAssignmentExtractor implements RoleAssignmentExtractor {

    private static final String OAUTH2_CLAIM_PREFERRED_USERNAME =
            "https://ror.entur.io/preferred_username";


    private static final String DEFAULT_ADMIN_ORG = "RB";



    private final UserService userService;

    public LocalBabaRoleAssignmentExtractor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<RoleAssignment> getRoleAssignmentsForUser(Authentication authentication) {

        if (
                !(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)
        ) {
            throw new AccessDeniedException("Not authenticated with token");
        }

        String preferredUserName = (String) jwtAuthenticationToken
                .getTokenAttributes()
                .get(OAUTH2_CLAIM_PREFERRED_USERNAME);

        // if the preferred userName is set, this is a user account
        if(preferredUserName != null) {
            return userService.roleAssignments(preferredUserName);
        }

        // otherwise this is a machine-to-machine token
        return parsePermissionsClaim(jwtAuthenticationToken
                .getTokenAttributes()
                .get(RoROAuth2Claims.OAUTH2_CLAIM_PERMISSIONS));
    }


    private List<RoleAssignment> parsePermissionsClaim(Object permissionsClaim) {
        if (permissionsClaim instanceof List claimPermissionAsList) {
            List<String> claimPermissionAsStringList = claimPermissionAsList;
            return claimPermissionAsStringList
                    .stream()
                    .map(role ->
                            RoleAssignment
                                    .builder()
                                    .withRole(role)
                                    .withOrganisation(DEFAULT_ADMIN_ORG)
                                    .build()
                    )
                    .toList();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported claim type: " + permissionsClaim
            );
        }
    }

}
