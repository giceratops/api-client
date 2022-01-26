package gh.giceratops.api.client.handler.http;

import gh.giceratops.api.client.ApiClient;
import gh.giceratops.api.client.ApiHandler;
import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.api.client.handler.http.auth.HttpAuthentication;
import gh.giceratops.jutil.concurrent.DaemonThreadFactory;

import javax.ws.rs.core.HttpHeaders;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

public class HttpHandler implements ApiHandler {

    private final HttpClient http;
    private final CookieManager cookieManager;
    private final HttpAuthentication authentication;

    public HttpHandler() {
        this.authentication = new HttpAuthentication();
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .cookieHandler(this.cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(Executors.newScheduledThreadPool(2, new DaemonThreadFactory()))
                .build();
    }

    public HttpAuthentication auth() {
        return this.authentication;
    }

    public HttpClient http() {
        return this.http;
    }

    public CookieManager cookies() {
        return this.cookieManager;
    }

    @Override
    public <I, O> HttpRequest<I, O> createRequest(final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        return new HttpRequest<>(this, method, endpoint, in, outClass)
                .reqHeader(HttpHeaders.USER_AGENT, ApiClient.class.getSimpleName());
    }
}
