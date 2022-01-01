package gh.giceratops.api.client;

@FunctionalInterface
public interface ApiHandler {

    <I, O> ApiRequest<I, O> createRequest(final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass);
}
