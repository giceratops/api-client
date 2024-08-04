package gh.giceratops.api.client.handler.nats;

import gh.giceratops.api.client.ApiURL;

public class NatsTopic extends ApiURL {

    public NatsTopic(final String topic) {
        super(String.format("nats://%s", topic));
    }
}
