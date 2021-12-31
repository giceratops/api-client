package gh.giceratops.api.client.protocols.file;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;

public class FileHandler implements ApiHandler {

    @Override
    public <I, O> FileRequest<I, O> createRequest(ApiClient client, ApiMethod method, I in, Class<O> outClass) {
        return null;
    }
}
