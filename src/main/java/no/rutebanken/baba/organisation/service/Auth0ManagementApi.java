package no.rutebanken.baba.organisation.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Auth0ManagementApi {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Clock clock = Clock.systemUTC();
    private final AuthAPI authAPI;
    private final String domain;

    private TokenHolder tokenHolder;
    private Instant accessTokenRetrievedAt;
    private ManagementAPI managementAPI;

    public Auth0ManagementApi(AuthAPI authAPI, String domain) {
        this.authAPI = authAPI;
        this.domain = domain;
    }

    public synchronized ManagementAPI getManagementAPI() throws Auth0Exception {
        if (managementAPI == null) {
            refreshToken();

            Auth0HttpClient auth0HttpClient = DefaultHttpClient.newBuilder()
                    .withConnectTimeout(10)
                    .withReadTimeout(10)
                    .withMaxRetries(10)
                    .build();

            managementAPI = ManagementAPI.newBuilder(domain, tokenHolder.getAccessToken())
                    .withHttpClient(auth0HttpClient)
                    .build();
        }
        if (hasTokenExpired()) {
            refreshToken();
            managementAPI.setApiToken(tokenHolder.getAccessToken());
        }
        return managementAPI;
    }

    /**
     * The token is considered expired 60 seconds before its actual expiration date.
     *
     * @return true if the token is about to expire.
     */
    private boolean hasTokenExpired() {
        return clock.instant().isAfter(accessTokenRetrievedAt.plus(tokenHolder.getExpiresIn() - 60, ChronoUnit.SECONDS));
    }

    private void refreshToken() throws Auth0Exception {
        logger.debug("Refreshing Admin API token");
        TokenRequest tokenRequest = authAPI.requestToken("https://" + domain + "/api/v2/");
        tokenHolder = tokenRequest.execute().getBody();
        accessTokenRetrievedAt = clock.instant();
    }
}
