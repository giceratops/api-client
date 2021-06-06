package gh.giceratops.api.client;

import gh.giceratops.jutil.Reflect;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "unchecked"})
public class ApiClient extends ApiConfigurable<ApiClient> {

    private final ApiRoutes routes;
    private final HttpClient http;
    private final CookieStore cookies;

    private BiConsumer<ApiClient, HttpRequest> onRequest;
    private BiConsumer<ApiResponse<?>, Throwable> listener;

    public ApiClient() {
        this(new ApiRoutes());
    }

    public ApiClient(final Consumer<ApiRoutes> setup) {
        this();
        setup.accept(this.routes);
    }

    private ApiClient(final ApiRoutes routes) {
        super();
        final var cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        this.routes = routes;
        this.cookies = cookieManager.getCookieStore();
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .executor(Executors.newScheduledThreadPool(2, runnable -> {
                    final var thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                }))
                .build();

        this.onRequest = (client, uri) -> {
        };

        super.reqHeader("User-Agent", ApiProperties.USER_AGENT)
                .reqHeader("Content-Type", "application/json");
    }

    public ApiClient copy() {
        return this.copy(false);
    }

    public ApiClient copy(boolean deep) {
        final var ret = new ApiClient(this.routes);
        if (deep) {
            ret.onRequest(ret.onRequest);
        }
        return ret;
    }

    public void onRequest(final BiConsumer<ApiClient, HttpRequest> onRequest) {
        this.onRequest = onRequest;
    }

    protected BiConsumer<ApiClient, HttpRequest> onRequest() {
        return this.onRequest;
    }

    public HttpClient http() {
        return this.http;
    }

    public CookieStore cookies() {
        return this.cookies;
    }

    BiConsumer<ApiResponse<?>, Throwable> listener() {
        return this.listener;
    }

    public Optional<Executor> executor() {
        return this.http.executor();
    }

    public Optional<ScheduledExecutorService> scheduledExecutor() {
        return this.executor()
                .filter(e -> e instanceof ScheduledExecutorService)
                .map(e -> (ScheduledExecutorService) e);
    }

    public ApiClient listener(final BiConsumer<ApiResponse<?>, Throwable> listener) {
        this.listener = this.listener == null ?
                listener : this.listener.andThen(listener);

        return this;
    }

    public ApiRoutes routes() {
        return this.routes;
    }

    private <I, O> ApiRequest<I, O> request(final ApiMethod method, final I in, final Class<O> outClass) {
        return new ApiRequest<>(this, method, in, outClass);
    }

    public <I, O> ApiRequest<I, O> GET(final Class<O> oClass) {
        return this.request(ApiMethod.GET, null, oClass);
    }

    public <I, O> ApiRequest<I, O> GET(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.GET, (I) null, oClass)
                .urlParam("id", id);
    }

    public <I, O> ApiRequest<O, O> POST(final O o) {
        return this.request(ApiMethod.POST, o, (Class<O>) o.getClass());
    }

    public <I, O> ApiRequest<I, O> POST(final I in, Class<O> outClass) {
        return this.request(ApiMethod.POST, in, outClass);
    }

    public <I, O> ApiRequest<O, O> PUT(final O o) {
        return this.request(ApiMethod.PUT, o, (Class<O>) o.getClass());
    }

    public <I, O> ApiRequest<I, O> PUT(final I in, Class<O> outClass) {
        return this.request(ApiMethod.PUT, in, outClass);
    }

    public <I, O> ApiRequest<I, O> DELETE(final Class<O> oClass) {
        return this.request(ApiMethod.DELETE, null, oClass);
    }

    public <I, O> ApiRequest<I, O> DELETE(final Class<O> oClass, final Object id) {
        return this.request(ApiMethod.DELETE, (I) null, oClass)
                .urlParam("id", id);
    }

    public <I, O> ApiRequest<I, Void> DELETE(final I in, final String id) {
        return this.request(ApiMethod.DELETE, in, Void.class)
                .urlParam(id, Reflect.getField(in, id));
    }
}
