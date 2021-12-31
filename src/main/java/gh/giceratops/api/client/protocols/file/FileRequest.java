package gh.giceratops.api.client.protocols.file;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiEndpoint;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.protocols.http.HttpHandler;

import java.util.concurrent.CompletableFuture;

public class FileRequest<I, O> implements ApiRequest<I, O> {

    private final ApiMethod method;
    private final ApiClient client;
    private final HttpHandler handler;
    private final I in;
    private final Class<O> outClass;
    private final Class<?> lookupClass;

    private long createdAt, finishedAt;
    private ApiEndpoint overrideEndpoint;

    public FileRequest(final ApiClient client, final HttpHandler handler, final ApiMethod method, final I in, final Class<O> outClass) {
        this(client, handler, method, in, outClass, outClass);
    }

    private FileRequest(final ApiClient client, final HttpHandler handler, final ApiMethod method, final I in, final Class<O> outClass, Class<?> lookupClass) {
        this.client = client;
        this.method = method;
        this.handler = handler;
        this.in = in;
        this.outClass = outClass;
        this.lookupClass = lookupClass;
    }

    @Override
    public <C> FileRequest<I, C> as(Class<C> cClass) {
        return new FileRequest<>(this.client, this.handler, this.method, this.in, cClass, this.lookupClass);
    }

    @Override
    public ApiMethod method() {
        return null;
    }

    @Override
    public ApiRequest<I, O> urlParam(final String param, final Object o) {
        return null;
    }

    @Override
    public CompletableFuture<FileResponse<O>> async() {
        return null;
    }

    @Override
    public CompletableFuture<FileResponse<O>> sync() {
        return null;
    }
}
