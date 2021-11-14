package gh.giceratops.api.client.auth;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface ApiAuthenticator extends Predicate<URI>, BiConsumer<URI, HttpRequest.Builder> {

}
