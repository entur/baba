package no.rutebanken.baba.security.permissionstore;


public interface PermissionStoreClient {

  /**
   * Return the email for a given user.
   */
  PermissionStoreUser getUser(
          String subject
  );

 }
