package gh.giceratops.api.client.endpoint;

import gh.giceratops.api.client.ApiConfigurable;

import java.time.Duration;

public class HttpEndpoint {

    protected final String url;
    protected Duration cacheTime;

    public HttpEndpoint(final String url) {
        this.url = url;
    }

    public String rawUrl() {
        return this.url;
    }

    public ApiService service(final String service) {
        return new ApiService(this.url, service);
    }

    public HttpEndpoint cache(final Duration cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public Duration cache() {
        return this.cacheTime;
    }

    public String url(final ApiConfigurable<?> configurable) {
        var url = this.rawUrl();
        if (configurable == null) {
            return url;
        } else {
            return configurable.apply(url);
        }
    }
}
