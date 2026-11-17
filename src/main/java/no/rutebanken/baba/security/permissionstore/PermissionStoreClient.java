package no.rutebanken.baba.security.permissionstore;

public interface PermissionStoreClient {
  /**
   * @return the user with this OAuth2 subject, or null if unknown.
   * @throws PermissionStoreUnavailableException if the permission store cannot be queried.
   */
  PermissionStoreUser getUser(String subject);
}
