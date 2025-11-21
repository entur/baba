package no.rutebanken.baba.organisation.service;

import no.rutebanken.baba.organisation.model.OrganisationException;

public class OAuth2UserNotFoundException extends OrganisationException {

  public OAuth2UserNotFoundException(String message) {
    super(message);
  }
}
