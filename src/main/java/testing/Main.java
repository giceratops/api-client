package testing;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.endpoint.HttpEndpoint;

public class Main {

    public static void main(String[] args) throws Throwable {
        final var apiClient = new ApiClient();
        apiClient.onRequest(new KeycloakHandler("giceratops", "password"));

        apiClient.routes()
                .POST(KeycloakAuth.Req.class, new HttpEndpoint("https://sso.syrup.ms/auth/realms/{realm}/protocol/openid-connect/token"))
                .GET(KeycloakAuth.UserInfo.class, new HttpEndpoint("https://sso.syrup.ms/auth/realms/{realm}/protocol/openid-connect/userinfo"));
//
//        apiClient.POST(new KeycloakAuth.Req("giceratops", "password"), KeycloakAuth.Resp.class)
//                .urlParam("realm", "syrup")
//                .sync()
//                .thenAccept((apiResponse) -> {
//                    System.out.println(apiResponse.body());
//                    final var bearer = apiResponse.body().access_token;
//                    apiClient.GET(KeycloakAuth.UserInfo.class)
//                            .urlParam("realm", "syrup")
//                            .reqHeader("Authorization", "Bearer " + bearer)
//                            .sync()
//                            .thenAccept((userInfoApiResponse) -> {
//                                System.out.println(userInfoApiResponse.body());
//                            });
//                }).exceptionally((t) -> {
//                    t.printStackTrace(System.err);
//                    return null;
//                });

        apiClient.GET(KeycloakAuth.UserInfo.class)
                .urlParam("realm", "syrup")
                .sync()
                .thenAccept((userInfoApiResponse) -> {
                    System.out.println(userInfoApiResponse.http().request().headers());
                    System.out.println(userInfoApiResponse.statusCode());
                    System.out.println(userInfoApiResponse.body());
                });
        apiClient.GET(KeycloakAuth.UserInfo.class)
                .urlParam("realm", "syrup")
                .sync()
                .thenAccept((userInfoApiResponse) -> {
                    System.out.println(userInfoApiResponse.http().request().headers());
                    System.out.println(userInfoApiResponse.statusCode());
                    System.out.println(userInfoApiResponse.body());
                });

        apiClient.GET(String.class)
                .override(new HttpEndpoint("https://whoami.syrup.ms"))
                .sync().thenAccept((stringApiResponse) -> {
                    System.out.println(stringApiResponse.body());
                });
    }
}
