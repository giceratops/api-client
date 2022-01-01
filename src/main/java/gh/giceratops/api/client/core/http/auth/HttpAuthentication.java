package gh.giceratops.api.client.core.http.auth;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class HttpAuthentication {

    private final List<HttpAuthenticator> authenticators;

    public HttpAuthentication() {
        this.authenticators = new LinkedList<>();
    }

    public void register(final HttpAuthenticator authenticator) {
        this.authenticators.add(authenticator);
    }

    public Optional<HttpAuthenticator> find(final URI uri) {
        return this.authenticators
                .stream()
                .filter((authenticator) -> authenticator.accepts(uri))
                .findFirst();
    }

    public void ifPresent(final URI uri, final Consumer<HttpAuthenticator> consumer) {
        this.find(uri).ifPresent(consumer);
    }
}
