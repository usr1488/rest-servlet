package rest.servlet.core.converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class XmlConverter implements RequestBodyConverter {
    @Override
    public <T> T convert(InputStream inputStream, Class<T> clazz) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[4096];
        int count;

        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        while ((count = inputStream.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, count));
        }

        return xmlMapper.readValue(stringBuilder.toString(), clazz);
    }

    @Override
    public List<String> supportedContentTypes() {
        return Arrays.asList(
                "text/xml",
                "application/xml",
                "application/xhtml+xml",
                "application/rss+xml"
        );
    }
}
