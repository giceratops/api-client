package gh.giceratops.api.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.NoSuchElementException;

class ApiRoutesTest {

    private static final class ClassA {
    }

    private static final class ClassB {
    }

    private ApiRoutes router;
    private ApiEndpoint endpoint;

    private ApiRoutesTest() {
    }

    @BeforeEach
    void beforeEach() {
        this.endpoint = new ApiEndpoint("");
        this.router = new ApiRoutes()
                .get(ClassA.class, endpoint)
                .post(ClassA.class, endpoint)
                .put(ClassA.class, endpoint)
                .delete(ClassA.class, endpoint);
    }

    @Test
    void add() {
        Assertions.assertThrows(NullPointerException.class, () -> this.router.add(null, ClassA.class, this.endpoint));
        Assertions.assertThrows(NullPointerException.class, () -> this.router.add(ApiMethod.GET, null, this.endpoint));
        Assertions.assertThrows(NullPointerException.class, () -> this.router.add(ApiMethod.GET, ClassA.class, null));
    }

    void testMethod(final ApiMethod method) {
        final var endpointA = router.endpoint(method, ClassA.class);
        final var endpointB = router.endpoint(method, ClassB.class);

        final Executable exeA = () -> {
            final var a = endpointA.orElseThrow(NoSuchElementException::new);
            Assertions.assertEquals(this.endpoint, a);
        };
        final Executable exeB = () -> {
            final var b = endpointB.orElseThrow(NoSuchElementException::new);
            Assertions.assertNotEquals(this.endpoint, b);
        };

        Assertions.assertDoesNotThrow(exeA);
        Assertions.assertThrows(NoSuchElementException.class, exeB);
    }

    @Test
    void GET() {
        testMethod(ApiMethod.GET);
    }

    @Test
    void POST() {
        testMethod(ApiMethod.POST);
    }

    @Test
    void PUT() {
        testMethod(ApiMethod.PUT);
    }

    @Test
    void DELETE() {
        testMethod(ApiMethod.DELETE);
    }
}
