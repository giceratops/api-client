package gh.giceratops.api.client.protocols.http;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.jutil.Maps;
import gh.giceratops.jutil.Strings;

import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "unchecked"})
public class HttpConfigurable<I extends HttpConfigurable<I>> implements Function<String, String> {

    private Map<String, String> headerParams;
    private Map<String, String> queryParams;
    private Map<String, String> urlParams;

    protected HttpConfigurable() {
    }

    protected HttpConfigurable(final HttpConfigurable<?> configurable) {
        if (configurable.headerParams != null) {
            this.headerParams().putAll(configurable.headerParams);
        }

        if (configurable.queryParams != null) {
            this.queryParams().putAll(configurable.queryParams);
        }

        if (configurable.urlParams != null) {
            this.urlParams().putAll(configurable.urlParams);
        }
    }

    private Map<String, String> headerParams() {
        if (this.headerParams == null) {
            this.headerParams = new HashMap<>();
        }
        return this.headerParams;
    }

    private Map<String, String> queryParams() {
        if (this.queryParams == null) {
            this.queryParams = new HashMap<>();
        }
        return this.queryParams;
    }

    private Map<String, String> urlParams() {
        if (this.urlParams == null) {
            this.urlParams = new HashMap<>();
        }
        return this.urlParams;
    }

    protected String[] headers() {
        if (this.headerParams == null) {
            return new String[]{HttpHeaders.USER_AGENT, ApiClient.class.getSimpleName()};
        }

        return this.headerParams.entrySet().stream()
                .collect(LinkedList<String>::new, (list, entry) -> {
                    list.add(entry.getKey());
                    list.add(entry.getValue());
                }, LinkedList::addAll)
                .toArray(new String[0]);
    }

    public I reqHeader(final String header, final Object value) {
        this.headerParams().put(header, String.valueOf(value));
        return (I) this;
    }

    public I urlParam(final String param, final Object value) {
        this.urlParams().put(param, String.valueOf(value));
        return (I) this;
    }

    public I queryParam(final String param, final Object value) {
        this.queryParams().put(param, String.valueOf(value));
        return (I) this;
    }

    public String apply(String url) {
        if (!Maps.isEmpty(this.urlParams)) {
            url = Strings.format(url, this.urlParams);
        }

        if (!Maps.isEmpty(this.queryParams)) {
            final var queryString = this.queryParams.entrySet()
                    .stream()
                    .map(e -> String.format("%s=%s", Strings.urlEncode(e.getKey()), Strings.urlEncode(e.getValue())))
                    .collect(Collectors.joining("&"));
            url = String.format("%s?%s", url, queryString);
        }
        return url;
    }
}

