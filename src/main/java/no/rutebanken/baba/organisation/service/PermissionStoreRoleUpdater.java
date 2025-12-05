package no.rutebanken.baba.organisation.service;

import java.util.Collection;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilityRoleAssignment;
import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.user.User;
import no.rutebanken.baba.organisation.util.RoleAssignmentMapper;
import org.entur.auth.permission.client.AuthorizeTenant;
import org.entur.auth.permission.client.model.Access;
import org.entur.auth.permission.client.model.Agreement;

public class PermissionStoreRoleUpdater implements RoleUpdater {

  private static final String ROLE_PREFIX = "ror-";

  private final AuthorizeTenant authorizeTenant;

  public PermissionStoreRoleUpdater(AuthorizeTenant authorizeTenant) {
    this.authorizeTenant = authorizeTenant;
  }

  @Override
  public void updateRoles(User user) {
    Collection<ResponsibilitySet> responsibilitySets = user.getResponsibilitySets();
    for (ResponsibilitySet responsibilitySet : responsibilitySets) {
      for (ResponsibilityRoleAssignment rra : responsibilitySet.getRoles()) {
        Agreement agreement = Agreement
          .builder()
          .organisationId(1L)
          .operation(ROLE_PREFIX + rra.getTypeOfResponsibilityRole().getPrivateCode())
          .responsibilityType("scope")
          .responsibilityKey(RoleAssignmentMapper.toAtr(rra))
          .responsibilityName(responsibilitySet.getName())
          .access(Access.LES)
          .build();
        authorizeTenant.storeAgreement(agreement);
      }
    }
  }
}
