package gh.giceratops.api.client.protocols.http;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

public class HttpHandler implements ApiHandler {

    private final HttpClient http;
    private final CookieManager cookieManager;

    public HttpHandler() {
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(this.cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(Executors.newScheduledThreadPool(2, new DaemonThreadFactory()))
                .build();

        // super.reqHeader(HttpHeaders.USER_AGENT, ApiClient.class.getSimpleName());
    }

    public HttpClient http() {
        return this.http;
    }

    @Override
    public <I, O> HttpRequest<I, O> createRequest(final ApiClient client, final ApiMethod method, final I in, final Class<O> outClass) {
        return new HttpRequest<>(client, this, method, in, outClass);
    }
}
