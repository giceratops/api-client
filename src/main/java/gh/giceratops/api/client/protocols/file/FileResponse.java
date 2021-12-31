package gh.giceratops.api.client.protocols.file;

import gh.giceratops.api.client.ApiResponse;

public class FileResponse<O> implements ApiResponse<O> {

    @Override
    public FileRequest<?, O> request() {
        return null;
    }

    @Override
    public O body() {
        return null;
    }
}
