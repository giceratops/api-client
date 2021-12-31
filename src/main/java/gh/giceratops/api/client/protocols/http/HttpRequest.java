package gh.giceratops.api.client.protocols.http;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.ApiEndpoint;
import gh.giceratops.api.client.protocols.http.handler.JsonBodyHandler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class HttpRequest<I, O> extends HttpConfigurable<HttpRequest<I, O>> implements ApiRequest<I, O> {

    private final ApiMethod method;
    private final ApiClient client;
    private final HttpHandler handler;
    private final I in;
    private final Class<O> outClass;
    private final Class<?> lookupClass;

    private long createdAt, finishedAt;

    public HttpRequest(final ApiClient client, final HttpHandler handler, final ApiMethod method, final I in, final Class<O> outClass) {
        this(client, handler, method, in, outClass, outClass);
    }

    private HttpRequest(final ApiClient client, final HttpHandler handler, final ApiMethod method, final I in, final Class<O> outClass, Class<?> lookupClass) {
        super(client);
        this.client = client;
        this.method = method;
        this.handler = handler;
        this.in = in;
        this.outClass = outClass;
        this.lookupClass = lookupClass;
    }

    public <C> HttpRequest<I, C> as(final Class<C> cClass) {
        return new HttpRequest<>(this.client, this.handler, this.method, this.in, cClass, this.lookupClass);
    }

    public ApiMethod method() {
        return this.method;
    }

    public long ping() {
        return this.finishedAt - this.createdAt;
    }

    private ApiEndpoint createEndpoint() {
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

    private java.net.http.HttpRequest createRequest(final ApiEndpoint endpoint) {
        final var builder = java.net.http.HttpRequest.newBuilder();
        final var uri = URI.create(endpoint.url(this));

        final java.net.http.HttpRequest.BodyPublisher publisher;
        final String contentType;
        if (this.in == null) {
            publisher = java.net.http.HttpRequest.BodyPublishers.noBody();
            contentType = MediaType.TEXT_PLAIN;
        } else if (this.in instanceof HttpFormData) {
            publisher = java.net.http.HttpRequest.BodyPublishers.ofString(((HttpFormData) this.in).asString());
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            publisher = java.net.http.HttpRequest.BodyPublishers.ofString(this.method.json().asString(this.in));
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


    private CompletableFuture<HttpResponse<O>> convert(final java.net.http.HttpRequest req, final CompletableFuture<java.net.http.HttpResponse<O>> future) {
        return future.thenApply((resp) -> {
            this.finishedAt = System.currentTimeMillis();
            return new HttpResponse<>(this, resp);
        });
    }

    @Override
    public CompletableFuture<HttpResponse<O>> async() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var future = this.handler.http().sendAsync(req, new JsonBodyHandler<>(this.method, this.outClass));
        return convert(req, future);
    }

    @Override
    public CompletableFuture<HttpResponse<O>> sync() {
        final var endpoint = this.createEndpoint();
        final var req = this.createRequest(endpoint);
        final var future = new CompletableFuture<java.net.http.HttpResponse<O>>();
        try {
            final var resp = this.handler.http().send(req, new JsonBodyHandler<>(this.method, this.outClass));
            future.complete(resp);
        } catch (final Throwable t) {
            future.completeExceptionally(t);
        }
        return convert(req, future);
    }
}
