package gh.giceratops.api.client;

import gh.giceratops.api.client.auth.ApiAuthentication;
import gh.giceratops.api.client.protocols.http.HttpConfigurable;
import gh.giceratops.jutil.Reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked"})
public class ApiClient extends HttpConfigurable<ApiClient> {

    private final ApiRoutes routes;

    private final Map<String, ApiHandler> handlers;
    private final ApiAuthentication authentication;

    public ApiClient(final Consumer<ApiRoutes> setup) {
        super();
        this.routes = new ApiRoutes();
        this.authentication = new ApiAuthentication();
        this.handlers = new HashMap<>();

        setup.accept(this.routes);
    }

    public ApiClient register(final String protocol, final ApiHandler handler) {
        this.handlers.put(protocol, handler);
        return this;
    }

    public ApiAuthentication auth() {
        return this.authentication;
    }

    public ApiRoutes routes() {
        return this.routes;
    }

    private <I, O> ApiRequest<I, O> request(final ApiMethod method, final I in, final Class<O> outClass) {
        final Class<?> aClass;
        if (in == null) {
            aClass = outClass;
        } else if (Class.class.equals(in.getClass())) {
            aClass = (Class<?>) in;
        } else {
            aClass = in.getClass();
        }

        return this.routes.endpoint(method, aClass)
                .map(ApiEndpoint::protocol)
                .map(this.handlers::get)
                .map((handler) -> handler.createRequest(this, method, in, outClass))
                .orElseThrow();
    }

    public <I, O> ApiRequest<I, O> get(final Class<O> oClass) {
        return this.request(ApiMethod.GET, null, oClass);
    }

    public <I, O> ApiRequest<?, O> get(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.GET, (I) null, oClass)
                .urlParam("id", id);
    }

    public <I, O> ApiRequest<O, O> post(final O o) {
        return this.request(ApiMethod.POST, o, (Class<O>) o.getClass());
    }

    public <I, O> ApiRequest<I, O> post(final I in, Class<O> outClass) {
        return this.request(ApiMethod.POST, in, outClass);
    }

    public <I, O> ApiRequest<O, O> put(final O o) {
        return this.request(ApiMethod.PUT, o, (Class<O>) o.getClass());
    }

    public <I, O> ApiRequest<O, O> put(final O o, final String id) {
        return this.request(ApiMethod.PUT, o, (Class<O>) o.getClass())
                .urlParam(id, Reflect.getField(o, id));
    }

    public <I, O> ApiRequest<I, O> put(final I in, Class<O> outClass) {
        return this.request(ApiMethod.PUT, in, outClass);
    }

    public <I, O> ApiRequest<I, O> delete(final Class<O> oClass) {
        return this.request(ApiMethod.DELETE, null, oClass);
    }

    public <I, O> ApiRequest<I, O> delete(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.DELETE, (I) null, oClass)
                .urlParam("id", id);
    }

    public <I, O> ApiRequest<I, Void> delete(final I in, final String id) {
        return this.request(ApiMethod.DELETE, in, Void.class)
                .urlParam(id, Reflect.getField(in, id));
    }
}
