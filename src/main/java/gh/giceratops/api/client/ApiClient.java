package gh.giceratops.api.client;

import gh.giceratops.api.client.handler.file.FileHandler;
import gh.giceratops.api.client.handler.http.HttpHandler;
import gh.giceratops.api.client.handler.rsx.ResourceHandler;
import gh.giceratops.jutil.Reflect;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;
import lombok.NonNull;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked"})
public class ApiClient {

    static {
        URL.setURLStreamHandlerFactory((protocol) -> switch (protocol) {
            case "rsx", "nats" -> new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) {
                    return new URLConnection(url) {
                        @Override
                        public void connect() {
                        }
                    };
                }

                @Override
                protected String toExternalForm(URL u) {
                    return u.getAuthority();
                }
            };
            default -> null;
        });
    }

    private final ScheduledExecutorService executor;
    private final Map<String, ApiHandler> handlers;
    private final ApiRoutes routes;

    public ApiClient() {
        this((Consumer<ApiRoutes>) null);
    }

    public ApiClient(final Consumer<ApiRoutes> setup) {
        this(Executors.newScheduledThreadPool(2, new DaemonThreadFactory()), setup);
    }

    public ApiClient(@NonNull final ScheduledExecutorService executor, final Consumer<ApiRoutes> setup) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.handlers = new HashMap<>();
        this.routes = new ApiRoutes();

        this.defaultHandlers();
        if (setup != null) {
            setup.accept(this.routes);
        }
    }

    private ApiClient(@NonNull final ApiClient other) {
        this.executor = other.executor;
        this.routes = other.routes;
        this.handlers = new HashMap<>();
        this.defaultHandlers();
    }

    private void defaultHandlers() {
        this.register("file", new FileHandler())
                .register(new String[]{"http", "https"}, new HttpHandler())
                .register("rsx", new ResourceHandler());
    }

    public ApiClient copy() {
        return new ApiClient(this);
    }

    public ScheduledExecutorService executor() {
        return this.executor;
    }

    public ApiRoutes routes() {
        return this.routes;
    }

    public ApiClient register(@NonNull final String[] protocols, final ApiHandler handler) {
        for (final var protocol : protocols) {
            this.register(protocol, handler);
        }
        return this;
    }

    public ApiClient register(@NonNull final String protocol, final ApiHandler handler) {
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

    public <H extends ApiHandler> Optional<H> handler(final Class<H> hClass) {
        return this.handlers.values()
                .stream()
                .filter((handler) -> handler.getClass().isAssignableFrom(hClass))
                .map((handler) -> (H) handler)
                .findFirst();
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
                .orElseThrow(() -> new NoSuchElementException("No endpoint to " + method + " " + aClass.getName()));
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
