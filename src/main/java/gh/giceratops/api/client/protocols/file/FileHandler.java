package gh.giceratops.api.client.protocols.file;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FileHandler implements ApiHandler {

    private final ScheduledExecutorService ses;

    public FileHandler() {
        this.ses = Executors.newScheduledThreadPool(2, new DaemonThreadFactory());
    }

    ScheduledExecutorService executor() {
        return this.ses;
    }

    @Override
    public <I, O> FileRequest<I, O> createRequest(ApiClient client, ApiMethod method, I in, Class<O> outClass) {
        return null;
    }
}
