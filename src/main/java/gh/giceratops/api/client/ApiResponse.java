package gh.giceratops.api.client;


import javax.ws.rs.core.Response.Status;

public interface ApiResponse<O> {

    ApiRequest<?, O> request();

    Status status();

    O body();

    default boolean isSuccess() {
        return this.status().getFamily() == Status.Family.SUCCESSFUL;
    }
}
