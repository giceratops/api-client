package gh.giceratops.api.client.endpoint;

import java.util.Objects;

@SuppressWarnings("unused")
public record ApiService(String baseUrl, String service) {

    public ApiService(final String baseUrl, final String service) {
        this.baseUrl = Objects.requireNonNull(baseUrl);
        this.service = Objects.requireNonNull(service);
    }

    public HttpEndpoint get() {
        return this.route(null);
    }

    public HttpEndpoint route(String path) {
        final String url;
        if (path == null) {
            url = String.format("%s/%s", this.baseUrl, this.service);
        } else {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            url = String.format("%s/%s/%s", this.baseUrl, this.service, path);
        }
        return new HttpEndpoint(url);
    }
}
