package gh.giceratops.api.client.endpoint;

import gh.giceratops.api.client.ApiConfigurable;

import java.time.Duration;

public class HttpEndpoint {

    protected final String url;
    protected Object staticResponse;

    public HttpEndpoint(final String url) {
        this.url = url;
    }

    public String rawUrl() {
        return this.url;
    }

    public ApiService service(final String service) {
        return new ApiService(this.url, service);
    }

    public HttpEndpoint staticResponse(final Object staticResponse) {
        this.staticResponse = staticResponse;
        return this;
    }

    public Object staticResponse() {
        return this.staticResponse;
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
