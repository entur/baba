package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.OrganisationException;

public class OAuth2RoleNotFoundException extends OrganisationException {

  public OAuth2RoleNotFoundException(String message) {
    super(message);
  }
}
