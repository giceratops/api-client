package gh.giceratops.api.client;

import gh.giceratops.api.client.auth.ApiAuthentication;
import gh.giceratops.jutil.Reflect;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;

import javax.ws.rs.core.HttpHeaders;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked"})
public class ApiClient extends ApiConfigurable<ApiClient> {

    private final HttpClient http;
    private final ApiRoutes routes;
    private final CookieManager cookieManager;
    private final ApiAuthentication authentication;

    public ApiClient() {
        this(new ApiRoutes());
    }

    public ApiClient(final Consumer<ApiRoutes> setup) {
        this();
        setup.accept(this.routes);
    }

    private ApiClient(final ApiRoutes routes) {
        super();
        this.routes = routes;
        this.authentication = new ApiAuthentication();
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(this.cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(Executors.newScheduledThreadPool(2, new DaemonThreadFactory()))
                .build();

        super.reqHeader(HttpHeaders.USER_AGENT, ApiClient.class.getSimpleName());
    }

    public ApiClient copy() {
        return new ApiClient(this.routes);
    }

    public ApiAuthentication auth() {
        return this.authentication;
    }

    public HttpClient http() {
        return this.http;
    }

    public CookieManager cookies() {
        return this.cookieManager;
    }

    public ApiRoutes routes() {
        return this.routes;
    }

    public Optional<Executor> executor() {
        return this.http.executor();
    }

    public Optional<ScheduledExecutorService> scheduledExecutor() {
        return this.executor()
                .filter(e -> e instanceof ScheduledExecutorService)
                .map(e -> (ScheduledExecutorService) e);
    }

    private <I, O> ApiRequest<I, O> request(final ApiMethod method, final I in, final Class<O> outClass) {
        return new ApiRequest<>(this, method, in, outClass);
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
