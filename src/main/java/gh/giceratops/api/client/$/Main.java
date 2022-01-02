package gh.giceratops.api.client.$;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiResponse;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.api.client.core.http.HttpHandler;
import gh.giceratops.api.client.core.http.HttpRequest;

import java.net.URI;

public class Main {

    public static void main(final String... args) throws Throwable {
        final var client = new ApiClient((routes) -> routes
                .get(Object.class, new ApiURL("https://{service}.{server}.ms"))
                .get(String.class, new ApiURL("file://{file}.json"))
        );
        System.out.println(client.routes());

        ((HttpHandler) client.handler("https"))
                .auth()
                .register((URI uri, java.net.http.HttpRequest.Builder builder) -> builder.header("X-Auth", "Auth"));


        ((HttpRequest<?, ?>) client.get(Object.class))
                .out(String.class)
                .reqHeader("X-Custom-Header1", "1") // Buggy
                .reqHeader("X-Custom-Header2", "2")
                .urlParam("service", "whoami")
                .urlParam("server", "syrup")
                .queryParam("q", "search")
                .queryParam("filter", "1")
                .queryParam("filter", "2")
                .async()
                .thenApply(ApiResponse::body)
                .thenAccept(System.out::println)
                .exceptionally((t) -> {
                    t.printStackTrace(System.err);
                    return null;
                })
                .join();

        client.get(String.class)
                .out(TestJson.class)
                .urlParam("file", "test2")
                .async()
                .thenApply(ApiResponse::body)
                .thenAccept(System.out::println)
                .exceptionally((t) -> {
                    t.printStackTrace(System.err);
                    return null;
                })
                .join();

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

    public record TestJson(int id, String name) {
    }
}
