package gh.giceratops.api.client;

import gh.giceratops.api.client.core.file.FileHandler;
import gh.giceratops.api.client.core.http.HttpHandler;
import gh.giceratops.jutil.Reflect;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked"})
public class ApiClient {

    private final ScheduledExecutorService executor;
    private final Map<String, ApiHandler> handlers;
    private final ApiRoutes routes;

    public ApiClient(@Nullable final Consumer<ApiRoutes> setup) {
        this(Executors.newScheduledThreadPool(2, new DaemonThreadFactory()), setup);
    }

    public ApiClient(@NotNull final ScheduledExecutorService executor, @Nullable final Consumer<ApiRoutes> setup) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.handlers = new HashMap<>();
        this.routes = new ApiRoutes();

        // default handlers
        this.register(new String[]{"http", "https"}, new HttpHandler());
        this.register("file", new FileHandler());

        if (setup != null) {
            setup.accept(this.routes);
        }
    }

    public ScheduledExecutorService executor() {
        return this.executor;
    }

    public ApiRoutes routes() {
        return this.routes;
    }

    public ApiClient register(@NotNull final String[] protocols, @Nullable final ApiHandler handler) {
        for (final var protocol : protocols) {
            this.register(protocol, handler);
        }
        return this;
    }

    public ApiClient register(@NotNull final String protocol, @Nullable final ApiHandler handler) {
        if (handler == null) {
            this.handlers.remove(protocol);
        } else {
            this.handlers.put(protocol, handler);
        }
        return this;
    }

    public ApiHandler handler(final String protocol) {
        return this.handlers.get(protocol);
    }

    private <I, O> ApiRequest<I, O> request(final ApiMethod method, final I in, final Class<O> outClass) {
        final Class<?> aClass;
        if (in == null) {
            aClass = outClass;
        } else if (in instanceof Class) {
            aClass = (Class<?>) in;
        } else {
            aClass = in.getClass();
        }

        final var endpoint = this.routes.endpoint(method, aClass);
        return endpoint
                .map(ApiURL::protocol)
                .map(this.handlers::get)
                .map((handler) -> handler.createRequest(method, endpoint.get(), in, outClass))
                .orElseThrow();
    }

    public <O> ApiRequest<?, O> get(final Class<O> oClass) {
        return this.request(ApiMethod.GET, null, oClass);
    }

    public <O> ApiRequest<?, O> get(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.GET, null, oClass)
                .urlParam("id", id);
    }

    public <O> ApiRequest<O, O> post(final O o) {
        return this.request(ApiMethod.POST, o, (Class<O>) o.getClass());
    }

    public <I, O> ApiRequest<I, O> post(final I in, Class<O> outClass) {
        return this.request(ApiMethod.POST, in, outClass);
    }

    public <O> ApiRequest<O, O> put(final O o) {
        return this.request(ApiMethod.PUT, o, (Class<O>) o.getClass());
    }

    public <O> ApiRequest<O, O> put(final O o, final String id) {
        return this.request(ApiMethod.PUT, o, (Class<O>) o.getClass())
                .urlParam(id, Reflect.getField(o, id));
    }

    public <I, O> ApiRequest<I, O> put(final I in, Class<O> outClass) {
        return this.request(ApiMethod.PUT, in, outClass);
    }

    public <O> ApiRequest<?, O> delete(final Class<O> oClass) {
        return this.request(ApiMethod.DELETE, null, oClass);
    }

    public <O> ApiRequest<?, O> delete(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.DELETE, null, oClass)
                .urlParam("id", id);
    }

    public <I> ApiRequest<I, Void> delete(final I in, final String id) {
        return this.request(ApiMethod.DELETE, in, Void.class)
                .urlParam(id, Reflect.getField(in, id));
    }
}
