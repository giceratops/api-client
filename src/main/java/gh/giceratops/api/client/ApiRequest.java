package gh.giceratops.api.client;

import java.util.concurrent.CompletableFuture;

public interface ApiRequest<I, O> {

    <C> ApiRequest<I, C> out(final Class<C> cClass);

    ApiURL url();

    ApiMethod method();

    ApiRequest<I, O> urlParam(final String param, final Object value);

    ApiRequest<I, O> headerParam(final String param, final Object value);

    CompletableFuture<? extends ApiResponse<O>> async();

    CompletableFuture<? extends ApiResponse<O>> sync();
}
