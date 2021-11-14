package gh.giceratops.api.client;

public record ApiEndpoint(String url) {

    public String rawUrl() {
        return this.url;
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
