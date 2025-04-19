package main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public static final String CONFIG_FILE_NAME = "notifications-%s.properties";
    private final Properties properties = new Properties();
    private final String envValue;

    public Config(String envValue) {
        checkValidEnvValues(envValue);
        this.envValue = envValue;
        loadPropertiesFile();
    }

    private void checkValidEnvValues(String envValue) {
        if (!envValue.equals("dev")
                && !envValue.equals("default")
                && !envValue.equals("test")
                && !envValue.equals("prod")) {
            throw new RuntimeException(envValue + " EnvValue not valid");
        }
    }

    public String gatewayHost() {
        return properties.getProperty("gateway.host");
    }

    public String gatewayPort() {
        return properties.getProperty("gateway.port");
    }

    public String uriScheme() {
        return properties.getProperty("uri.scheme");
    }

    public String salesRequestPath() {
        return properties.getProperty("sales.request.path");
    }

    public String queueName() {
        return properties.getProperty("queue.rabbimq.queue.name");
    }

    public String rabbitMQHost() {
        return properties.getProperty("queue.rabbitmq.host");
    }

    public String rabbitMQUsername() {
        return properties.getProperty("queue.rabbitmq.username");
    }

    public String rabbitMQPassword() {
        return properties.getProperty("queue.rabbitmq.password");
    }

    private void loadPropertiesFile() {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(formattedConfigFileName())) {
            if (input == null) {
                throw new IllegalArgumentException(formattedConfigFileName() + " not found in classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error trying to load " + CONFIG_FILE_NAME, e);
        }
    }

    private String formattedConfigFileName() {
        return CONFIG_FILE_NAME.formatted(this.envValue);
    }
}
