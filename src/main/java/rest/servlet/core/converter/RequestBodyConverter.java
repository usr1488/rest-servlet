package rest.servlet.core.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RequestBodyConverter {
    <T> T convert(InputStream inputStream, Class<T> clazz) throws IOException;
    List<String> supportedContentTypes();
}
