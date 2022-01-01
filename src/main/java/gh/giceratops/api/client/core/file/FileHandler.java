package gh.giceratops.api.client.core.file;

import gh.giceratops.api.client.ApiURL;
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
    public <I, O> FileRequest<I, O> createRequest(final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        return new FileRequest<I, O>(this, method, endpoint, in, outClass);
    }
}
