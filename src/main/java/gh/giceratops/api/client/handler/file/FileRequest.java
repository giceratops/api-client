package gh.giceratops.api.client.handler.file;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.jutil.Maps;
import gh.giceratops.jutil.Strings;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FileRequest<I, O> implements ApiRequest<I, O> {

    private final ApiMethod method;
    private final FileHandler handler;
    private final ApiURL endpoint;
    private final I in;

    private Class<?> outClass;
    private long createdAt, finishedAt;
    private Map<String, String> urlParams;

    public FileRequest(final FileHandler handler, final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        this.method = method;
        this.handler = handler;
        this.endpoint = endpoint;
        this.in = in;
        this.outClass = outClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> FileRequest<I, C> out(Class<C> cClass) {
        this.outClass = cClass;

        return (FileRequest<I, C>) this;
    }

    @Override
    public ApiURL url() {
        return null;
    }

    @Override
    public ApiMethod method() {
        return null;
    }

    @Override
    public FileRequest<I, O> urlParam(final String param, final Object o) {
        if (this.urlParams == null) {
            this.urlParams = new HashMap<>();
        }
        this.urlParams.put(param, String.valueOf(o));
        return this;
    }

    @Override
    public CompletableFuture<FileResponse<O>> async() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.doMethod();
            } catch (final Throwable t) {
                throw new RuntimeException(t);
            }
        }, this.handler.executor());
    }

    @Override
    public CompletableFuture<FileResponse<O>> sync() {
        final var future = new CompletableFuture<FileResponse<O>>();
        try {
            future.complete(doMethod());
        } catch (final Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    private FileResponse<O> doMethod() throws Throwable {
        return switch (this.method) {
            case GET -> this.doGet();
            case PUT -> this.doPut();
            case POST -> this.doPost();
            case DELETE -> this.doDelete();
        };
    }

    @SuppressWarnings("unchecked")
    private FileResponse<O> doGet() throws Throwable {
        var path = this.endpoint.path();
        if (!Maps.isEmpty(this.urlParams)) {
            path = Strings.format(path, this.urlParams);
        }
        try (final var reader = new FileReader(path)) {
            final var obj = this.method.json().asObject(reader, (Class<O>) this.outClass);
            return new FileResponse<>(this, Response.Status.OK, obj);
        }
    }

    //TODO
    private FileResponse<O> doPut() {
        throw new NotSupportedException();
    }

    //TODO
    private FileResponse<O> doPost() {
        throw new NotSupportedException();
    }

    //TODO
    private FileResponse<O> doDelete() {
        throw new NotSupportedException();
    }
}
