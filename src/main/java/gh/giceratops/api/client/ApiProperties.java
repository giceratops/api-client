package gh.giceratops.api.client;

import gh.giceratops.jutil.properties.OverrideProperty;

import java.time.Duration;

public class ApiProperties {

    public static final String USER_AGENT = String.format("%s :: %s", ApiClient.class.getSimpleName(), "1.0.0");

    public static final OverrideProperty<Duration> TIMEOUT = new OverrideProperty<>(
            "connectTimeout",
            Duration.ofSeconds(30),
            (string) -> Duration.ofSeconds(Integer.parseInt(string)),
            (duration) -> String.valueOf(duration.getSeconds())
    );
}
