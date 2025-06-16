package no.rutebanken.baba.config;

import java.util.List;
import no.rutebanken.baba.organisation.user.UserService;
import org.entur.ror.permission.AuthenticatedUser;
import org.entur.ror.permission.BabaRoleAssignmentExtractor;
import org.rutebanken.helper.organisation.RoleAssignment;

/**
 * Role assignment extractor used locally in Baba. This implementation retrieves the role assignments directly from the
 * Baba database. Other components use RemoteBabaRoleAssignmentExtractor to retrieve the role assignments through the Baba
 * user endpoint.
 */
class LocalBabaRoleAssignmentExtractor extends BabaRoleAssignmentExtractor {

  private final UserService userService;

  public LocalBabaRoleAssignmentExtractor(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected List<RoleAssignment> userRoleAssignments(AuthenticatedUser authenticatedUser) {
    return userService.roleAssignments(authenticatedUser);
  }
}
