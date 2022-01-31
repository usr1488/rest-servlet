package rest.servlet.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            PROPERTIES.load(
                    Optional.ofNullable(inputStream)
                            .orElse(
                                    new InputStream() {
                                        @Override
                                        public int read() {
                                            return -1;
                                        }
                                    }
                            )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return (String) PROPERTIES.get(key);
    }

    public static Properties getAllProperties() {
        return PROPERTIES;
    }
}
