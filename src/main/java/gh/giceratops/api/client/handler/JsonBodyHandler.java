package gh.giceratops.api.client.handler;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.jutil.Exceptions;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public record JsonBodyHandler<T>(ApiMethod method, Class<T> tClass) implements HttpResponse.BodyHandler<T> {

    @Override
    public HttpResponse.BodySubscriber<T> apply(final HttpResponse.ResponseInfo responseInfo) {
        if (String.class.equals(this.tClass)) {
            // noinspection unchecked
            return (HttpResponse.BodySubscriber<T>) HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        } else {
            return new ByteBodySubscriber<>((bytes) -> {
                try (final var reader = new InputStreamReader(new ByteArrayInputStream(bytes))) {
                    return this.method.json().asObject(reader, this.tClass);
                } catch (final Exception e) {
                    throw Exceptions.runtime(e, "Exception parsing info (%s)", responseInfo);
                }
            });
        }
    }
}
