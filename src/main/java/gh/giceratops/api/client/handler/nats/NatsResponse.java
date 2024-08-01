package gh.giceratops.api.client.handler.nats;

import gh.giceratops.api.client.ApiResponse;
import gh.giceratops.api.client.handler.rsx.ResourceRequest;

import javax.ws.rs.core.Response.Status;

public class NatsResponse<O> implements ApiResponse<O> {

    private final NatsRequest<?, O> request;
    private final Status status;
    private final O body;

    public NatsResponse(final NatsRequest<?, O> request, final Status status, final O body) {
        this.request = request;
        this.status = status;
        this.body = body;
    }

    @Override
    public NatsRequest<?, O> request() {
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
