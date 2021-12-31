package gh.giceratops.api.client.protocols.http.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ByteBodySubscriber<T> implements HttpResponse.BodySubscriber<T> {

    private final Function<byte[], T> parser;
    private final ByteArrayOutputStream baos;
    private final CompletableFuture<T> result;
    private final AtomicBoolean subscribed;
    private Flow.Subscription subscription;

    ByteBodySubscriber(final Function<byte[], T> parser) {
        this.parser = parser;
        this.baos = new ByteArrayOutputStream();
        this.result = new CompletableFuture<>();
        this.subscribed = new AtomicBoolean();
    }

    @Override
    public CompletionStage<T> getBody() {
        return this.result;
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        if (subscribed.compareAndSet(false, true)) {
            this.subscription = subscription;
            this.subscription.request(1);
        } else {
            subscription.cancel();
        }
    }

    @Override
    public void onNext(final List<ByteBuffer> items) {
        items.forEach(item -> {
            final var buf = new byte[item.remaining()];
            item.get(buf);
            this.baos.writeBytes(buf);
        });
        this.subscription.request(1);
    }

    @Override
    public void onError(final Throwable throwable) {
        this.result.completeExceptionally(new Throwable(this.baos.toString(), throwable));
    }

    @Override
    public void onComplete() {
        try {
            this.result.complete(this.parser.apply(this.baos.toByteArray()));
            this.baos.close();
        } catch (final IOException throwable) {
            this.result.completeExceptionally(throwable);
        }
    }
}
