package gh.giceratops.api.client.handler.nats;

import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;
import io.nats.client.Connection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NatsHandler implements ApiHandler {

    private final Connection nats;
    private final ScheduledExecutorService ses;

    public NatsHandler(Connection nats) {
        this.nats = nats;
        this.ses = Executors.newScheduledThreadPool(2, new DaemonThreadFactory());
    }

    @Override
    public <I, O> NatsRequest<I, O> createRequest(ApiMethod method, ApiURL endpoint, I in, Class<O> outClass) {
        return new NatsRequest<>(this, method, endpoint, in, outClass);
    }

    ScheduledExecutorService executor() {
        return this.ses;
    }

    Connection nats() {
        return this.nats;
    }

}
