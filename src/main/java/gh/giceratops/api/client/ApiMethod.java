package gh.giceratops.api.client;

import gh.giceratops.jutil.Json;

public enum ApiMethod {

    GET,
    PUT,
    POST,
    DELETE;

    private final Json json;

    ApiMethod() {
        this.json = new Json();
    }

    public Json json() {
        return this.json;
    }
}
