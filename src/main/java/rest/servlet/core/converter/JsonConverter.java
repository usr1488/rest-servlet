package rest.servlet.core.converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class JsonConverter implements RequestBodyConverter {
    private final ObjectMapper objectMapper;

    public JsonConverter() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    @Override
    public <T> T convert(InputStream inputStream, Class<T> clazz) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[4096];
        int count;

        while ((count = inputStream.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, count));
        }

        return objectMapper.readValue(stringBuilder.toString(), clazz);
    }

    @Override
    public List<String> supportedContentTypes() {
        return Arrays.asList(
                "application/json",
                "application/javascript"
        );
    }
}
