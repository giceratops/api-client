package gh.giceratops.api.client;

public interface ApiHandler {

    <I, O> ApiRequest<I, O> createRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass);
}
