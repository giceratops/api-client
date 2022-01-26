package gh.giceratops.api.client.handler.http.auth;

import java.net.URI;
import java.net.http.HttpRequest;

@FunctionalInterface
public interface HttpAuthenticator {

    default boolean accepts(final URI uri) {
        return true;
    }

    void handleRequest(final URI uri, final HttpRequest.Builder builder);

}
