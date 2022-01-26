package gh.giceratops.api.client.handler.file;

import gh.giceratops.api.client.ApiResponse;

import javax.ws.rs.core.Response.Status;

public class FileResponse<O> implements ApiResponse<O> {

    private final FileRequest<?, O> request;
    private final Status status;
    private final O body;

    public FileResponse(final FileRequest<?, O> request, final Status status, final O body) {
        this.request = request;
        this.status = status;
        this.body = body;
    }

    @Override
    public FileRequest<?, O> request() {
        return this.request;
    }

    @Override
    public Status status() {
        return this.status;
    }

    @Override
    public O body() {
        return this.body;
    }
}
