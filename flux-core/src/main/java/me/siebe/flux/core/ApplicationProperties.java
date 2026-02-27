package me.siebe.flux.core;

import me.siebe.flux.core.system.StartupBanner;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class, LoggingCategories.APPLICATION);
    private static final String APPLICATION_PROPERTIES = "application.properties";

    private static Properties properties;

    // Property keys matching application.properties file
    public static final String ENGINE_VERSION = "engine.version";
    public static final String LWJGL_VERSION = "lwjgl.version";


    static {
        try {
            loadApplicationProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadApplicationProperties() throws IOException {
        try (InputStream inputStream = StartupBanner.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            if (inputStream == null) {
                logger.warn("Application properties file not found");
                return;
            }

            properties = new Properties();
            properties.load(inputStream);

        } catch (IOException e) {
            logger.error("Error loading application properties file!", e);
            throw e;
        }
    }

    public static String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
