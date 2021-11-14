package testing;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiEndpoint;
import gh.giceratops.jutil.Json;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static record UserInfo(String sub, boolean email_verified, String preferred_username) {

        @Override
        public String toString() {
            return Json.stringify(this, true);
        }
    }

    public static void main(String[] args) throws Throwable {
        final var client = createClient(args[0], args[1]);
        testKeycloak(client);
    }

    private static ApiClient createClient(final String username, final String password) {
        final var client = new ApiClient((routes) -> routes
                .GET(UserInfo.class, new ApiEndpoint("https://sso.syrup.ms/auth/realms/{realm}/protocol/openid-connect/userinfo"))
        );
        final var keycloak = new Keycloak.Authenticator(client)
                .login(username, password);

        client.auth().register(keycloak);
        return client;
    }

    private static void testKeycloak(final ApiClient client) {
        Executors.newScheduledThreadPool(5).scheduleAtFixedRate(() -> {
            client.GET(UserInfo.class)
                    .urlParam("realm", "syrup")
                    .sync()
                    .thenAccept((resp) -> {
                        System.out.println(resp.http().request().headers());
                        System.out.println(resp.statusCode() + " " + resp.body());
                    });
        }, 0, 120, TimeUnit.SECONDS);
    }
}
