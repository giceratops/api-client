package gh.giceratops.api.client.core.http;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.api.client.core.http.handler.JsonBodyHandler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class HttpRequest<I, O> extends HttpConfigurable<HttpRequest<I, O>> implements ApiRequest<I, O> {

    private final ApiMethod method;
    private final HttpHandler handler;
    private final ApiURL endpoint;
    private final I in;

    private Class<?> outClass; // Actually Class<O> but needs to be a wildcard
    private long createdAt, finishedAt;

    public HttpRequest(final HttpHandler handler, final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        this.method = method;
        this.handler = handler;
        this.endpoint = endpoint;
        this.in = in;
        this.outClass = outClass;
    }

    @SuppressWarnings("unchecked")
    public <C> HttpRequest<I, C> out(final Class<C> cClass) {
        this.outClass = cClass;
        return (HttpRequest<I, C>) this;
    }

    public ApiMethod method() {
        return this.method;
    }

    public long ping() {
        return this.finishedAt - this.createdAt;
    }

    @Override
    public ApiURL url() {
        return this.endpoint;
    }

    private java.net.http.HttpRequest createRequest(final ApiURL endpoint) {
        final var builder = java.net.http.HttpRequest.newBuilder();

        final var uri = URI.create(this.apply(endpoint.url()));

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

        this.handler.auth().ifPresent(uri, (authenticator) -> authenticator.handleRequest(uri, builder));

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
    @SuppressWarnings("unchecked")
    public CompletableFuture<HttpResponse<O>> async() {
        final var endpoint = this.url();
        final var req = this.createRequest(endpoint);
        final var future = this.handler.http().sendAsync(req, new JsonBodyHandler<>(this.method, (Class<O>) this.outClass));
        return convert(req, future);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<HttpResponse<O>> sync() {
        final var endpoint = this.url();
        final var req = this.createRequest(endpoint);
        final var future = new CompletableFuture<java.net.http.HttpResponse<O>>();
        try {
            final var resp = this.handler.http().send(req, new JsonBodyHandler<>(this.method, (Class<O>) this.outClass));
            future.complete(resp);
        } catch (final Throwable t) {
            future.completeExceptionally(t);
        }
        return convert(req, future);
    }
}
