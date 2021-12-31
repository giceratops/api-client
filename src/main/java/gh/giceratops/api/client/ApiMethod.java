package gh.giceratops.api.client;

import gh.giceratops.jutil.Json;

public enum ApiMethod {

    GET,        // get
    PUT,        // update
    POST,       // create
    DELETE;     // remove

    private final Json json;

    ApiMethod() {
        this.json = new Json();
    }

    public Json json() {
        return this.json;
    }
}
