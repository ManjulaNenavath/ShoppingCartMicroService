package utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Config.properties from the test classpath once and serves values.
 *
 * Senior-tester note: keep environment data OUT of code. This reader is the single
 * choke point, so switching from local to a hosted URL is a one-line change in
 * Config.properties (or a -D system property override) - never a code edit.
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("Config.properties")) {
            if (in == null) {
                throw new IllegalStateException("Config.properties not found on classpath");
            }
            PROPERTIES.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Config.properties", e);
        }
    }

    private ConfigReader() {
    }

    /**
     * Returns a property. A matching -D system property wins, so you can override
     * any value at run time, e.g. -Duser.baseUrl=https://user.onrender.com
     */
    public static String get(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }
}
