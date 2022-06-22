package gh.giceratops.api.client.handler.http.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.api.client.handler.http.HttpFormData;
import gh.giceratops.jutil.Exceptions;
import gh.giceratops.jutil.Json;

import javax.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Keycloak {

    public static String CLIENT_ID;
    public static String CLIENT_SECRET;

    public record Token(String grantType, String username, String password) implements HttpFormData {

        static Token refresh(String username, String refreshToken) {
            return new Token("refresh_token", username, refreshToken);
        }

        static Token password(String username, String password) {
            return new Token("password", username, password);
        }

        @Override
        public Map<String, String> formData() {
            final var data = new HashMap<String, String>();
            if (grantType.equals("password")) {
                data.put("username", this.username);
            }
            data.put("grant_type", this.grantType);
            data.put(this.grantType, this.password);
            data.put("client_id", CLIENT_ID);
            data.put("client_secret", CLIENT_SECRET);
            return data;
        }

        @Override
        public String toString() {
            return Json.stringify(this, true);
        }
    }

    private record TokenResponse(String access_token,
                                 int expires_in,
                                 String refresh_token,
                                 int refresh_expires_in,
                                 String token_type,
                                 String session_state,
                                 String scope) {

        @Override
        public String toString() {
            return Json.stringify(this, true);
        }
    }

    public static class Authenticator implements HttpAuthenticator {

        private final ApiClient client;
        private String username;
        private String accessToken, refreshToken;
        private LocalDateTime accessTokenExpiresAt, refreshTokenExpiresAt;
        private DecodedJWT decodedJWT;
        private ScheduledFuture<?> future;

        public Authenticator(final ApiClient client) {
            this.client = client;
            this.client.routes()
                    .post(Keycloak.Token.class, new ApiURL("https://sso.syrup.ms/auth/realms/{realm}/protocol/openid-connect/token"));
        }

        public Authenticator login(final String username, final String password) {
            this.fetchToken(Token.password(username, password));
            this.username = username;
            return this;
        }

        @Override
        public boolean accepts(final URI uri) {
            return uri.getHost().endsWith("syrup.ms") ||
                    uri.getHost().equals("localhost");
        }

        @Override
        public void handleRequest(final URI uri, final HttpRequest.Builder builder) {
            if (this.accessTokenExpiresAt != null && LocalDateTime.now().isAfter(this.accessTokenExpiresAt)) {
                this.accessToken = null;
            }

            this.fetchTokensIfNecessary(uri);
            if (this.accessToken != null) {
                builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + this.accessToken);

                if (uri.toString().contains("USER_ID")) {
                    builder.uri(URI.create(
                            uri.toString().replace("USER_ID", this.decodedJWT.getSubject())
                    ));
                }
            }
        }

        private void fetchTokensIfNecessary(final URI uri) {
            if (this.username == null) return;
            if (this.accessToken != null) return;
            if (this.isRefreshUri(uri)) return;

            this.fetchToken();
        }

        private void fetchToken() {
            this.fetchToken(Token.refresh(this.username, this.refreshToken));
        }

        private void fetchToken(final Token token) {
            try {
                final var authResp = this.client.post(token, TokenResponse.class)
                        .urlParam("realm", "syrup")
                        .sync()
                        .get()
                        .body();

                this.accessToken = authResp.access_token;
                this.refreshToken = authResp.refresh_token;
                this.accessTokenExpiresAt = LocalDateTime.now().plusSeconds(authResp.expires_in);
                this.refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(authResp.refresh_expires_in);
                this.decodedJWT = JWT.decode(this.accessToken);

                this.scheduleRefresh(authResp.refresh_expires_in);
            } catch (final Exception e) {
                throw Exceptions.runtime(e, "Unable to fetch access token");
            }
        }

        private void scheduleRefresh(final int seconds) {
            if (this.future != null) {
                this.future.cancel(false);
            }

            this.client.executor()
                    .schedule((Runnable) this::fetchToken, seconds, TimeUnit.SECONDS);
        }

        private boolean isRefreshUri(final URI uri) {
            return uri.toString().contains("/protocol/openid-connect/token");
        }
    }
}
