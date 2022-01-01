package gh.giceratops.api.client.core.http;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiResponse;

import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public record HttpResponse<O>(HttpRequest<?, O> request,
                              java.net.http.HttpResponse<O> httpResponse) implements ApiResponse<O> {

    public java.net.http.HttpResponse<O> http() {
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

    @Override
    public Status status() {
        return Status.fromStatusCode(this.httpResponse.statusCode());
    }

    @Override
    public O body() {
        return this.httpResponse.body();
    }

    public long ping() {
        return this.request.ping();
    }
}
