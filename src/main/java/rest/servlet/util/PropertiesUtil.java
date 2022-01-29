package rest.servlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");

        if (stream == null) {
            throw new RuntimeException("Couldn't find application.properties in classpath");
        }

        try {
            PROPERTIES.load(stream);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return (String) PROPERTIES.get(key);
    }
}
