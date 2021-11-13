package testing;

import gh.giceratops.api.client.ApiClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.function.BiConsumer;

public class KeycloakHandler implements BiConsumer<ApiClient, HttpRequest> {

    private final String username, password;
    private String access_token, refresh_token;

    public KeycloakHandler(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void accept(final ApiClient client, final HttpRequest req) {
        if (this.access_token == null && !this.isRefreshingTokens(req.uri())) {
            try {
                this.access_token = client.POST(new KeycloakAuth.Req(this.username, this.password), KeycloakAuth.Resp.class)
                        .urlParam("realm", "syrup")
                        .get().access_token;
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        }
        System.out.println("Adding bearer");
        this.addBearerIfNeeded(client, req);
    }

    private void addBearerIfNeeded(final ApiClient client, final HttpRequest req) {
        if (this.isBearerNeeded(req.uri())) {
            // TODO add this to the request only - not the client
            // this is a security problem!!
            client.reqHeader("Authorization", "Bearer " + this.access_token);
        }
    }

    private boolean isBearerNeeded(final URI uri) {
        return uri.getHost().endsWith("sso.syrup.ms");
    }

    private boolean isRefreshingTokens(final URI uri) {
        return uri.toString().contains("/protocol/openid-connect/token");
    }
}
