package gh.giceratops.api.client.handler;

import gh.giceratops.api.client.ApiMethod;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private final ApiMethod method;
    private final Class<T> tClass;

    public JsonBodyHandler(final ApiMethod method, final Class<T> tClass) {
        this.method = method;
        this.tClass = tClass;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(final HttpResponse.ResponseInfo responseInfo) {
        if (String.class.equals(this.tClass)) {
            // noinspection unchecked
            return (HttpResponse.BodySubscriber<T>) HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        } else {
            return new ByteBodySubscriber<>((bytes) -> {
                try (final var reader = new InputStreamReader(new ByteArrayInputStream(bytes))) {
                    return this.method.mapper().readValue(reader, this.tClass);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
