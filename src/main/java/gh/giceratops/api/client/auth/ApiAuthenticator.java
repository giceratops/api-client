package gh.giceratops.api.client.auth;

import java.net.URI;
import java.net.http.HttpRequest;

public interface ApiAuthenticator {

    boolean accepts(final URI uri);

    void handleRequest(final URI uri, final HttpRequest.Builder builder);

}
