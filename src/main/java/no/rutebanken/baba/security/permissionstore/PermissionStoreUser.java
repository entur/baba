package no.rutebanken.baba.security.permissionstore;

public class PermissionStoreUser {

    public String subject;
    public String authority;
    public int organisationId;
    public String email;
    public boolean isClient;

    @Override
    public String toString() {
        return "PermissionStoreUser{" +
                "subject='" + subject + '\'' +
                ", authority='" + authority + '\'' +
                ", organisationId=" + organisationId +
                ", email='" + email + '\'' +
                ", isClient=" + isClient +
                '}';
    }
}
