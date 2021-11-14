package gh.giceratops.api.client;

import gh.giceratops.api.client.handler.JsonBodyHandler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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
    private final Class<?> lookupClass;

    private long createdAt, finishedAt;
    private ApiEndpoint overrideEndpoint;

    public ApiRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass) {
        this(client, method, in, outClass, outClass);
    }

    private ApiRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass, Class<?> lookupClass) {
        super(client);
        this.client = client;
        this.method = method;
        this.in = in;
        this.outClass = outClass;
        this.lookupClass = lookupClass;
    }

    public <C> ApiRequest<I, C> as(final Class<C> cClass) {
        return new ApiRequest<>(this.client, this.method, this.in, cClass, this.lookupClass);
    }

    public ApiMethod method() {
        return this.method;
    }

    public long ping() {
        return this.finishedAt - this.createdAt;
    }

    public ApiRequest<I, O> at(final ApiEndpoint endpoint) {
        this.overrideEndpoint = endpoint;
        return this;
    }

    private ApiEndpoint createEndpoint() {
        if (this.overrideEndpoint != null) {
            return this.overrideEndpoint;
        }

        final Class<?> aClass;
        if (this.in == null) {
            aClass = this.lookupClass;
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
            contentType = MediaType.TEXT_PLAIN;
        } else if (this.in instanceof ApiFormData) {
            publisher = HttpRequest.BodyPublishers.ofString(((ApiFormData) this.in).asString());
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            publisher = HttpRequest.BodyPublishers.ofString(this.method.json().asString(this.in));
            contentType = MediaType.APPLICATION_JSON;
        }

        builder.method(this.method.name(), publisher)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .uri(uri)
                .headers(super.headers());

        this.client.auth().ifPresent(uri, (authenticator) -> authenticator.handleRequest(uri, builder));

        this.createdAt = System.currentTimeMillis();
        return builder.build();
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
}
