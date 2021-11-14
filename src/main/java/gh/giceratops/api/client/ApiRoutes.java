package gh.giceratops.api.client;

import gh.giceratops.jutil.Pair;
import gh.giceratops.jutil.Strings;

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

    public ApiRoutes get(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.GET, clazz, endpoint);
    }

    public ApiRoutes post(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.POST, clazz, endpoint);
    }

    public ApiRoutes put(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.PUT, clazz, endpoint);
    }

    public ApiRoutes delete(final Class<?> clazz, final ApiEndpoint endpoint) {
        return this.add(ApiMethod.DELETE, clazz, endpoint);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("ApiRoutes (").append(this.routes.size()).append(")\r\n");
        this.routes.forEach((key, value) -> sb
                .append("\t- ")
                .append(Strings.padStart(key.left().name(), 6, ' '))
                .append(" :: ")
                .append(Strings.padEnd(key.right().getName(), 30, ' '))
                .append(" @ ")
                .append(value.url())
                .append("\r\n")
        );
        return sb.toString();
    }
}
