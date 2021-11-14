package gh.giceratops.api.client;

import gh.giceratops.jutil.Strings;

import java.util.Map;
import java.util.stream.Collectors;

public interface ApiFormData {

    Map<String, String> formData();

    default String asString() {
        return this.formData()
                .entrySet()
                .stream()
                .map((e) -> String.format("%s=%s", Strings.urlEncode(e.getKey()), Strings.urlEncode(e.getValue())))
                .collect(Collectors.joining("&"));
    }
}
