package gh.giceratops.api.client.handler.rsx;

import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ResourceHandler implements ApiHandler {

    private final ScheduledExecutorService ses;

    public ResourceHandler() {
        this.ses = Executors.newScheduledThreadPool(2, new DaemonThreadFactory());
    }

    ScheduledExecutorService executor() {
        return this.ses;
    }

    @Override
    public <I, O> ResourceRequest<I, O> createRequest(final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        return new ResourceRequest<>(this, method, endpoint, in, outClass);
    }
}
