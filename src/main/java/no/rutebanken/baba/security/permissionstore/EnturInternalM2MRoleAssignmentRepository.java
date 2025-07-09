package no.rutebanken.baba.security.permissionstore;

import com.google.common.collect.Streams;
import org.entur.ror.permission.AuthenticatedUser;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Build role assignments for Entur Internal machine-to-machine tokens.
 * Role assignments are built from configuration (application.properties) and from the "permissions" claim in the JWT token.
 * This is a temporary solution before migrating to Permission Store.
 * TODO Permission Store migration
 */
public class EnturInternalM2MRoleAssignmentRepository {

    static final String DEFAULT_ADMIN_ORG = "RB";
    static final String CLIENT_SUBJECT_SUFFIX = "@clients";

    /**
     * Map between a client-id and a comma-separated list of codespaces that this client is authorized to download.
     */
    private final Map<String, String> authorizedClientForRawNetexFileDownload;

    public EnturInternalM2MRoleAssignmentRepository(Map<String, String> authorizedClientForRawNetexFileDownload) {
        this.authorizedClientForRawNetexFileDownload = authorizedClientForRawNetexFileDownload == null ? Map.of() : authorizedClientForRawNetexFileDownload;
    }

    /**
     * Extract RoleAssignments from configuration and from the permission claim.
     */
    public List<RoleAssignment> getRolesAssignments(AuthenticatedUser authenticatedUser) {
        validateM2MClient(authenticatedUser);
        Stream<RoleAssignment> rolesFromConfiguration = getRoleAssignmentsFromConfiguration(authenticatedUser);
        Stream<RoleAssignment> rolesFromPermissions = getRoleAssignmentsFromPermissions(authenticatedUser);
        return Streams.concat(rolesFromConfiguration, rolesFromPermissions).toList();
    }

    private void validateM2MClient(AuthenticatedUser authenticatedUser) {
        if (!authenticatedUser.isClient()) {
            throw new IllegalArgumentException("The user is not a machine-to-machine client: " + authenticatedUser.subject());
        }
    }

    /**
     * Internal clients (from Entur Internal) owned by RoR contain cross-organization roles under the permission claim.
     */
    private static Stream<RoleAssignment> getRoleAssignmentsFromPermissions(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.permissions()
                .stream()
                .map(role ->
                        RoleAssignment
                                .builder()
                                .withRole(role)
                                .withOrganisation(DEFAULT_ADMIN_ORG)
                                .build()
                );
    }

    /**
     * Internal clients (from Entur Internal) owned by other teams may be explicitly configured with role assignments.
     */
    private Stream<RoleAssignment> getRoleAssignmentsFromConfiguration(AuthenticatedUser authenticatedUser) {
        return getAuthorizedCodespacesForM2MClient(clientId(authenticatedUser.subject()))
                .stream()
                .map(codespace -> RoleAssignment
                        .builder()
                        .withOrganisation(codespace)
                        .withRole(AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW)
                        .build());
    }


    /**
     * Return the list of codespaces for which the m2m client can download raw NeTEx files.
     */
    private List<String> getAuthorizedCodespacesForM2MClient(String clientId) {
        String authorizedCodespaces = authorizedClientForRawNetexFileDownload.get(clientId);
        if (!StringUtils.hasText(authorizedCodespaces)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(authorizedCodespaces.split(","));
        }

    }

    /**
     * Extract the OAuth2 client-id from the subject claim.
     * The subject claim structure for m2m clients is "client-id@clients"
     */
    private String clientId(String subject) {
        return subject.replace(CLIENT_SUBJECT_SUFFIX, "");
    }

}
