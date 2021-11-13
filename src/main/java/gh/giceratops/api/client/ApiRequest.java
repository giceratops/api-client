package gh.giceratops.api.client;

import gh.giceratops.api.client.handler.JsonBodyHandler;
import gh.giceratops.api.client.endpoint.HttpEndpoint;
import gh.giceratops.jutil.Strings;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ApiRequest<I, O> extends ApiConfigurable<ApiRequest<I, O>> {

    private final ApiMethod method;
    private final ApiClient client;
    private final I in;
    private final Class<O> outClass;

    private long createdAt, finishedAt;
    private HttpEndpoint overrideEndpoint;

    public ApiRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass) {
        super(client);
        this.client = client;
        this.method = method;
        this.in = in;
        this.outClass = outClass;
    }

    private HttpEndpoint createEndpoint() {
        if (this.overrideEndpoint != null) {
            return this.overrideEndpoint;
        }

        final Class<?> c;
        if (this.in == null) {
            c = this.outClass;
        } else if (Class.class.equals(this.in.getClass())) {
            c = (Class<?>) this.in;
        } else {
            c = this.in.getClass();
        }
        return this.client.routes().endpoint(this.method, c)
                .orElseThrow();
    }

    private HttpRequest createRequest(final HttpEndpoint endpoint) {
        final var builder = HttpRequest.newBuilder();
        final var url = endpoint.url(this);

        HttpRequest.BodyPublisher publisher;
        try {
            if (this.in == null) {
                publisher = HttpRequest.BodyPublishers.noBody();
            } else if (this.in instanceof ApiFormData) {
                final var s = ((ApiFormData) this.in).formData().entrySet()
                        .stream()
                        .map(e -> String.format("%s=%s", Strings.urlEncode(e.getKey()), Strings.urlEncode(e.getValue())))
                        .collect(Collectors.joining("&"));
                builder.header("Content-Type", "application/x-www-form-urlencoded");
                publisher = HttpRequest.BodyPublishers.ofString(s);
            } else {
                builder.header("Content-Type", "application/json");
                publisher = HttpRequest.BodyPublishers.ofString(this.method.json().asString(this.in));
            }
        } catch (final Exception e) {
            publisher = HttpRequest.BodyPublishers.noBody();
        }

        final var uri = URI.create(url);
        builder.method(this.method.name(), publisher)
                .uri(uri)
                .headers(super.headers());

        this.createdAt = System.currentTimeMillis();

        final var req = builder.build();
        this.client.onRequest().accept(this.client, req);

        return req;
    }

    private ApiResponse<O> createResponse(HttpResponse<O> response) {
        this.finishedAt = System.currentTimeMillis();
        return new ApiResponse<>(this, response);
    }

    public ApiMethod method() {
        return this.method;
    }

    public long ping() {
        return this.finishedAt - this.createdAt;
    }

    private CompletableFuture<ApiResponse<O>> convert(final HttpRequest req, final CompletableFuture<HttpResponse<O>> future) {
        var f = future.thenApply(this::createResponse);
        if (this.client.listener() != null) {
            f = f.whenComplete(this.client.listener());
        }
        return f;
    }

    private CompletableFuture<ApiResponse<O>> fromCache(final HttpEndpoint endpoint, final HttpRequest req) {
        final var staticResponse = endpoint.staticResponse();
        if (staticResponse != null) {
            final var future = new CompletableFuture<ApiResponse<O>>();
            final var resp = new ApiResponse<>(this, new HttpResponse<>() {
                @Override
                public int statusCode() {
                    return 200;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<O>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public O body() {
                    return (O) staticResponse;
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public HttpClient.Version version() {
                    return null;
                }
            });
            future.complete(resp);
            return future;
        }
        return null;
    }

    public CompletableFuture<ApiResponse<O>> async() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var cached = this.fromCache(endpoint, req);
        if (cached != null) {
            return cached;
        }

        final var future = this.client.http().sendAsync(req, new JsonBodyHandler<>(this.method, this.outClass));
        return convert(req, future);
    }

    public CompletableFuture<ApiResponse<O>> sync() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var cached = this.fromCache(endpoint, req);
        if (cached != null) {
            return cached;
        }

        final var future = new CompletableFuture<HttpResponse<O>>();
        try {
            final var resp = this.client.http().send(req, new JsonBodyHandler<>(this.method, this.outClass));
            future.complete(resp);
        } catch (final Throwable t) {
            future.completeExceptionally(t);
        }
        return convert(req, future);
    }

    public ApiRequest<I, O> override(final HttpEndpoint endpoint) {
        this.overrideEndpoint = endpoint;
        return this;
    }

    public O get() throws Exception {
        return this.sync().get().body();
    }

    public O get(final HttpEndpoint endpoint) throws Exception {
        return this.override(endpoint).get();
    }
}
