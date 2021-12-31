package gh.giceratops.api.client;

public interface ApiResponse<O> {

    ApiRequest<?, O> request();

    O body();
}
