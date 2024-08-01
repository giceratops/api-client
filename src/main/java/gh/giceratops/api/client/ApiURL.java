package gh.giceratops.api.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ApiURL {

    private final URL url;

    public ApiURL(final String url) {
        try {
            this.url = new URL(url);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Not a valid URL", e);
        }
    }

    public String url() {
        return this.url.toExternalForm();
    }

    public String path() {
        return this.url.getPath();
    }

    public String protocol() {
        return this.url.getProtocol();
    }

    @Override
    public String toString() {
        return String.format("ApiURL[url=%s, protocol=%s, path=%s]", this.url, this.protocol(), this.path());
    }
}
