package me.siebe.flux.util.io.handlers;

import me.siebe.flux.util.io.FileFormat;
import me.siebe.flux.util.io.FileHandler;
import me.siebe.flux.util.io.FileIOException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * {@link FileHandler} implementation that serialises and deserialises data using the YAML file format.
 */
public class YamlFileHandler implements FileHandler {

    private final Yaml yaml;

    /**
     * Creates a handler using the default SnakeYAML configuration tuned for human-readable output.
     */
    public YamlFileHandler() {
        this(createDefaultYaml());
    }

    /**
     * Creates a handler using the provided {@link Yaml} instance.
     *
     * @param yaml pre-configured YAML processor
     */
    public YamlFileHandler(Yaml yaml) {
        this.yaml = Objects.requireNonNull(yaml, "yaml");
    }

    @Override
    public FileFormat getFormat() {
        return FileFormat.YAML;
    }

    @Override
    public <T> T read(Path path, Class<T> targetType) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(targetType, "targetType");

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            T result = yaml.loadAs(reader, targetType);
            if (result == null) {
                throw new FileIOException("YAML file '" + path + "' is empty or contains null content.");
            }
            return result;
        } catch (IOException exception) {
            throw new FileIOException("Failed to read YAML file: " + path, exception);
        } catch (RuntimeException exception) {
            throw new FileIOException("Failed to parse YAML content from: " + path, exception);
        }
    }

    @Override
    public <T> T parse(String content, Class<T> targetType) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(targetType, "targetType");

        try {
            T result = yaml.loadAs(content, targetType);
            if (result == null) {
                throw new FileIOException("YAML content is empty or contains null content.");
            }
            return result;
        } catch (RuntimeException exception) {
            throw new FileIOException("Failed to parse YAML content", exception);
        }
    }

    @Override
    public void write(Path path, Object data) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(data, "data");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            yaml.dump(data, writer);
        } catch (IOException exception) {
            throw new FileIOException("Failed to write YAML file: " + path, exception);
        } catch (RuntimeException exception) {
            throw new FileIOException("Failed to serialise YAML content for: " + path, exception);
        }
    }

    private static Yaml createDefaultYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        loaderOptions.setProcessComments(true);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(dumperOptions);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        return new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }
}


