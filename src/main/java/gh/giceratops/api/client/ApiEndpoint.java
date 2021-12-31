package gh.giceratops.api.client;

import gh.giceratops.api.client.protocols.http.HttpConfigurable;

import java.net.MalformedURLException;
import java.net.URL;

public record ApiEndpoint(String url) {

    public String url(final HttpConfigurable<?> configurable) {
        if (configurable == null) {
            return this.url;
        } else {
            return configurable.apply(this.url);
        }
    }

    public String protocol() {
        try {
            return new URL(this.url).getProtocol();
        } catch (MalformedURLException ignore) {
            return "";
        }
    }
}
