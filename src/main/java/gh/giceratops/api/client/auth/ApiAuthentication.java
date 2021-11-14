package gh.giceratops.api.client.auth;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ApiAuthentication {

    private final List<ApiAuthenticator> authenticators;

    public ApiAuthentication() {
        this.authenticators = new LinkedList<>();
    }

    public void register(final ApiAuthenticator authenticator) {
        this.authenticators.add(authenticator);
    }

    public Optional<ApiAuthenticator> find(final URI uri) {
        return this.authenticators
                .stream()
                .filter((authenticator) -> authenticator.test(uri))
                .findFirst();
    }
}
