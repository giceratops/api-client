package gh.giceratops.api.client.handler.nats;

import gh.giceratops.api.client.ApiMethod;
import gh.giceratops.api.client.ApiRequest;
import gh.giceratops.api.client.ApiURL;
import gh.giceratops.jutil.Maps;
import gh.giceratops.jutil.Strings;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NatsRequest<I, O> implements ApiRequest<I, O> {

    private static final byte[] EMPTY_REQUEST = new byte[]{};

    private final ApiMethod method;
    private final NatsHandler handler;
    private final ApiURL endpoint;
    private final I in;

    private Class<?> outClass;
    private long createdAt, finishedAt;
    private Map<String, String> urlParams;
    private Map<String, String> headerParams;

    public NatsRequest(final NatsHandler handler, final ApiMethod method, final ApiURL endpoint, final I in, final Class<O> outClass) {
        this.method = method;
        this.handler = handler;
        this.endpoint = endpoint;
        this.in = in;
        this.outClass = outClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> NatsRequest<I, C> out(Class<C> cClass) {
        this.outClass = cClass;

        return (NatsRequest<I, C>) this;
    }

    @Override
    public ApiURL url() {
        return this.endpoint;
    }

    @Override
    public ApiMethod method() {
        return this.method;
    }

    @Override
    public NatsRequest<I, O> urlParam(final String param, final Object o) {
        if (this.urlParams == null) {
            this.urlParams = new HashMap<>();
        }
        this.urlParams.put(param, String.valueOf(o));
        return this;
    }

    @Override
    public NatsRequest<I, O> headerParam(final String param, final Object o) {
        if (this.headerParams == null) {
            this.headerParams = new HashMap<>();
        }
        this.headerParams.put(param, String.valueOf(o));
        return this;
    }

    private String createTopic() {
        var topic = this.endpoint.url();
        if (!Maps.isEmpty(this.urlParams)) {
            topic = Strings.format(topic, this.urlParams);
        }
        return topic;
    }

    private byte[] createInput() {
        if (this.in == null) {
            return EMPTY_REQUEST;
        }
        return this.method().json().asBytes(this.in);
    }

    private Message createMessage() {
        final var headers = new Headers();
        headers.add("method", this.method.name());
        if (this.headerParams != null) {
            this.headerParams.forEach(headers::add);
        }
        return NatsMessage.builder()
                .subject(this.createTopic())
                .headers(headers)
                .data(this.createInput())
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<NatsResponse<O>> async() {
        return this.handler.nats().request(this.createMessage()).thenApply((msg) -> {
            try {
                final var obj = this.method.json().asObject(msg.getData(), (Class<O>) this.outClass);
                msg.ack();
                return new NatsResponse<>(this, Response.Status.OK, obj);
            } catch (Throwable t) {
                msg.nak();
                throw t;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<NatsResponse<O>> sync() {
        Message msg = null;
        final var future = new CompletableFuture<NatsResponse<O>>();
        try {
            msg = this.handler.nats().request(this.createMessage(), Duration.ofSeconds(10));
            final var obj = this.method.json().asObject(msg.getData(), (Class<O>) this.outClass);
            msg.ack();
            future.complete(new NatsResponse<>(this, Response.Status.OK, obj));
        } catch (final Throwable t) {
            if (msg != null) {
                msg.nak();
            }
            future.completeExceptionally(t);
        }
        return future;
    }
}
