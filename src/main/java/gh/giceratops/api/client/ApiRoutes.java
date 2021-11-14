package gh.giceratops.api.client;

import gh.giceratops.jutil.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ApiRoutes {

    private final Map<Pair<ApiMethod, Class<?>>, ApiEndpoint> routes;

    ApiRoutes() {
        this.routes = new HashMap<>();
    }

    ApiRoutes add(final ApiMethod method, final Class<?> clazz, final ApiEndpoint endpoint) {
        this.routes.put(Pair.nonNull(method, clazz), Objects.requireNonNull(endpoint));
        return this;
    }

    Optional<ApiEndpoint> endpoint(final ApiMethod method, final Class<?> clazz) {
        return this.routes.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(method, clazz))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    public ApiRoutes GET(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.GET, clazz, endpoint);
    }

    public ApiRoutes POST(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.POST, clazz, endpoint);
    }

    public ApiRoutes PUT(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.PUT, clazz, endpoint);
    }

    public ApiRoutes DELETE(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.DELETE, clazz, endpoint);
    }
}
