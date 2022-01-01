package gh.giceratops.api.client.core.file;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.ApiURL;

import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FileRequest<I, O> implements ApiRequest<I, O> {

    private final ApiMethod method;
    private final FileHandler handler;
    private final ApiURL endpoint;
    private final I in;

    private Class<?> outClass;
    private long createdAt, finishedAt;

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
    public ApiRequest<I, O> urlParam(final String param, final Object o) {
        return null;
    }

    @Override
    public CompletableFuture<FileResponse<O>> async() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<FileResponse<O>> sync() {
        final var future = new CompletableFuture<FileResponse<O>>();
        final var path = this.endpoint.path();
        try (final var reader = new FileReader(path)) {
            final var obj = this.method.json().asObject(reader, (Class<O>) this.outClass);
            future.complete(new FileResponse<>(Response.Status.OK, obj));
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
