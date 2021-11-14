package gh.giceratops.api.client;

import gh.giceratops.api.client.handler.JsonBodyHandler;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class ApiRequest<I, O> extends ApiConfigurable<ApiRequest<I, O>> {

    private final ApiMethod method;
    private final ApiClient client;
    private final I in;
    private final Class<O> outClass;

    private long createdAt, finishedAt;
    private ApiEndpoint overrideEndpoint;

    public ApiRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass) {
        super(client);
        this.client = client;
        this.method = method;
        this.in = in;
        this.outClass = outClass;
    }

    private ApiEndpoint createEndpoint() {
        if (this.overrideEndpoint != null) {
            return this.overrideEndpoint;
        }

        final Class<?> aClass;
        if (this.in == null) {
            aClass = this.outClass;
        } else if (Class.class.equals(this.in.getClass())) {
            aClass = (Class<?>) this.in;
        } else {
            aClass = this.in.getClass();
        }
        return this.client.routes()
                .endpoint(this.method, aClass)
                .orElseThrow();
    }

    private HttpRequest createRequest(final ApiEndpoint endpoint) {
        final var builder = HttpRequest.newBuilder();
        final var uri = URI.create(endpoint.url(this));

        final HttpRequest.BodyPublisher publisher;
        final String contentType;
        if (this.in == null) {
            publisher = HttpRequest.BodyPublishers.noBody();
            contentType = "text/plain";
        } else if (this.in instanceof ApiFormData) {
            publisher = HttpRequest.BodyPublishers.ofString(((ApiFormData) this.in).asString());
            contentType = "application/x-www-form-urlencoded";
        } else {
            publisher = HttpRequest.BodyPublishers.ofString(this.method.json().asString(this.in));
            contentType = "application/json";
        }

        builder.method(this.method.name(), publisher)
                .header("Content-Type", contentType)
                .uri(uri)
                .headers(super.headers());

        this.client.auth()
                .find(uri)
                .ifPresent((authenticator) ->
                        authenticator.accept(uri, builder)
                );

        this.createdAt = System.currentTimeMillis();
        return builder.build();
    }

    public ApiMethod method() {
        return this.method;
    }

    public long ping() {
        return this.finishedAt - this.createdAt;
    }

    private CompletableFuture<ApiResponse<O>> convert(final HttpRequest req, final CompletableFuture<HttpResponse<O>> future) {
        return future.thenApply((resp) -> {
            this.finishedAt = System.currentTimeMillis();
            return new ApiResponse<>(this, resp);
        });
    }

    public CompletableFuture<ApiResponse<O>> async() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var future = this.client.http().sendAsync(req, new JsonBodyHandler<>(this.method, this.outClass));
        return convert(req, future);
    }

    public CompletableFuture<ApiResponse<O>> sync() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var future = new CompletableFuture<HttpResponse<O>>();
        try {
            final var resp = this.client.http().send(req, new JsonBodyHandler<>(this.method, this.outClass));
            future.complete(resp);
        } catch (final Throwable t) {
            future.completeExceptionally(t);
        }
        return convert(req, future);
    }

    public ApiRequest<I, O> override(final ApiEndpoint endpoint) {
        this.overrideEndpoint = endpoint;
        return this;
    }

    public O get() throws Exception {
        return this.sync().get().body();
    }

    public O get(final ApiEndpoint endpoint) throws Exception {
        return this.override(endpoint).get();
    }
}
