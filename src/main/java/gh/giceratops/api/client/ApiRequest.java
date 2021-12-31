package gh.giceratops.api.client;

import java.util.concurrent.CompletableFuture;

public interface ApiRequest<I, O> {

    <C> ApiRequest<I, C> as(final Class<C> cClass);

    ApiMethod method();

    ApiRequest<I, O> urlParam(final String param, final Object o);

    CompletableFuture<? extends ApiResponse<O>> async();

    CompletableFuture<? extends ApiResponse<O>> sync();
}
