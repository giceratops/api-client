package gh.giceratops.api.client;

public record ApiEndpoint(String url) {

    public String url(final ApiConfigurable<?> configurable) {
        if (configurable == null) {
            return this.url;
        } else {
            return configurable.apply(this.url);
        }
    }
}
