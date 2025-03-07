package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.responsibility.ResponsibilitySet;
import no.rutebanken.baba.organisation.model.responsibility.Role;
import no.rutebanken.baba.organisation.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class NoAuthenticationIamService implements IamService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean createOrUpdate(User user) {
        logger.info("Authentication disabled! Ignored createOrUpdate: {}", user.getUsername());
        return false;
    }

    @Override
    public void removeUser(User user) {
        logger.info("Authentication disabled! Ignored removeUser: {}", user.getUsername());
    }

    @Override
    public void createRole(Role role) {
        logger.info("Authentication disabled! Ignored createRole: {}", role.getId());
    }

    @Override
    public void removeRole(Role role) {
        logger.info("Authentication disabled! Ignored removeRole: {}", role.getId());
    }

    @Override
    public void updateResponsibilitySet(ResponsibilitySet responsibilitySet) {
        logger.info("Authentication disabled! Ignored updateResponsibilitySet: {}", responsibilitySet.getName());
    }

}
