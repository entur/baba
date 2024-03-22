package no.rutebanken.baba.permissionstore.service;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class DefaultPermissionStoreService implements PermissionStoreService {
    private final PermissionStoreClient permissionStoreClient;

    public DefaultPermissionStoreService(PermissionStoreClient permissionStoreClient) {
        this.permissionStoreClient = permissionStoreClient;
    }

    @Override
    public boolean isFederated(String email) {
        Objects.requireNonNull(email);
        String domain = StringUtils.substringAfter(email, "@");
        return permissionStoreClient.isFederated(domain);
    }


}
