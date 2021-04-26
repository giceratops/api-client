package gh.giceratops.api.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

public enum ApiMethod {

    GET(new ObjectMapper()),
    PUT(new ObjectMapper()),
    POST(new ObjectMapper()),
    DELETE(new ObjectMapper());

    private final ObjectMapper mapper;

    ApiMethod(final ObjectMapper mapper) {
        this.mapper = mapper
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDateFormat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
    }

    public ObjectMapper mapper() {
        return this.mapper;
    }
}
