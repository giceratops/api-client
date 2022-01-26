package gh.giceratops.api.client.handler.rsx;

import gh.giceratops.api.client.ApiResponse;

import javax.ws.rs.core.Response.Status;

public class ResourceResponse<O> implements ApiResponse<O> {

    private final ResourceRequest<?, O> request;
    private final Status status;
    private final O body;

    public ResourceResponse(final ResourceRequest<?, O> request, final Status status, final O body) {
        this.request = request;
        this.status = status;
        this.body = body;
    }

    @Override
    public ResourceRequest<?, O> request() {
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
