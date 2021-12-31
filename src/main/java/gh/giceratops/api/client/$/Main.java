package gh.giceratops.api.client.$;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiEndpoint;
import gh.giceratops.api.client.protocols.http.HttpHandler;
import gh.giceratops.api.client.protocols.http.HttpRequest;

public class Main {

    public static void main(final String... args) throws Throwable {
        System.out.printf("Hello%n");

        final var httpHandler = new HttpHandler();
        final var client = new ApiClient((routes) -> routes
                .get(Object.class, new ApiEndpoint("http://whoami.syrup.ms"))
        ).register("http", httpHandler)
                .register("https", httpHandler);

        final var req = client.get(Object.class);


        System.out.println(
                ((HttpRequest<Object, Object>) req)
                        .reqHeader("X-Custom-Header1", "1") // Buggy
                        .as(String.class)
                        .reqHeader("X-Custom-Header2", "2")
                        .sync()
                        .get()
                        .body()
        );

        // ApiClient.register(protocol, ApiProtocolHandler);
        // ApiClient.routes().register(Object, ApiEndpoint);

        /*
         * client.get(Object)   // ApiRequest --> HttpRequest | FileRequest ...
         *      .reqParam(String, String)
         *      [.as(____Request)]
         *      [...]
         *      .async()        // CompletableFuture<ApiResponse<O>>
         *      .get()          // ApiResponse<O>
         *          - .body()         // O
         *          - .status()       // int (cfr. http status code)
         *          - .millis()
         */
    }
}
