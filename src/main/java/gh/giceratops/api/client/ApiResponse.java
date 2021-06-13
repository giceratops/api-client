package gh.giceratops.api.client;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public record ApiResponse<O>(ApiRequest<?, O> request,
                             HttpResponse<O> httpResponse) {

    public HttpResponse<O> http() {
        return this.httpResponse;
    }

    public URI uri() {
        return this.httpResponse.uri();
    }

    public ApiMethod method() {
        return this.request.method();
    }

    public List<URI> redirects() {
        final var list = new LinkedList<URI>();
        var resp = this.httpResponse;
        do {
            list.add(resp.uri());
            resp = resp.previousResponse().orElse(null);
        } while (resp != null);

        Collections.reverse(list);
        return list;
    }

    public boolean isSuccess() {
        return this.httpResponse.statusCode() >= 200
                && this.httpResponse.statusCode() < 300;
    }

    public int statusCode() {
        return this.httpResponse.statusCode();
    }

    public O body() {
        return this.httpResponse.body();
    }

    public long ping() {
        return this.request.ping();
    }
}
