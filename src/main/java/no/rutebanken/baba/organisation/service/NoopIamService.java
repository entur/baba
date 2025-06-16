package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOOP Implementation of the IAM service.
 */
public class NoopIamService implements IamService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void createUser(User user) {
    logger.info("NOOP IAM service! Ignored createUser: {}", user.getUsername());
  }

  @Override
  public void updateUser(User user) {
    logger.info("NOOP IAM service! Ignored updateUser: {}", user.getUsername());
  }

  @Override
  public void resetPassword(User user) {
    logger.info("NOOP IAM service! Ignored resetPassword: {}", user.getUsername());
  }

  @Override
  public void removeUser(User user) {
    logger.info("NOOP IAM service! Ignored removeUser: {}", user.getUsername());
  }

  @Override
  public void createRole(Role role) {
    logger.info("NOOP IAM service! Ignored createRole: {}", role.getId());
  }

  @Override
  public void removeRole(Role role) {
    logger.info("NOOP IAM service! Ignored removeRole: {}", role.getId());
  }

  @Override
  public void updateResponsibilitySet(ResponsibilitySet responsibilitySet) {
    logger.info(
      "NOOP IAM service! Ignored updateResponsibilitySet: {}",
      responsibilitySet.getName()
    );
  }

  @Override
  public boolean hasUserWithEmail(String email) {
    return false;
  }

  @Override
  public String getUserWithEmail(String email) {
    return "";
  }
}
