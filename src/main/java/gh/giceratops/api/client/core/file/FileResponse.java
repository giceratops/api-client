package gh.giceratops.api.client.core.file;

import gh.giceratops.api.client.ApiResponse;

import javax.ws.rs.core.Response.Status;

public class FileResponse<O> implements ApiResponse<O> {

    private final Status status;
    private final O body;

    public FileResponse(final Status status, final O body) {
        this.status = status;
        this.body = body;
    }

    @Override
    public FileRequest<?, O> request() {
        return null;
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
