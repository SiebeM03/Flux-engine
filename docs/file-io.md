# File IO Utilities

The Flux API ships with a lightweight, extensible file IO system that abstracts reading and writing configuration
files (starting with YAML) while remaining open for additional formats.

## Core Types

- `FileFormat`: value object describing a logical format (name + recognised extensions). The static `FileFormat.YAML`
  instance covers `.yaml` and `.yml`.
- `FileHandler`: strategy interface for serialising/deserialising a given `FileFormat`. Implementations translate
  between on-disk data and domain objects.
- `YamlFileHandler`: default handler backed by SnakeYAML, registered automatically by `FileIOManager.createDefault()`.
- `FileIOManager`: registry and façade that selects the right handler for a given path or explicitly supplied format.
- `FileIOException`: unchecked wrapper for IO/serialisation failures thrown by handlers and the manager.

## Obtaining a Manager

```java
FileIOManager io = FileIOManager.createDefault();
```

- Registers `YamlFileHandler`; call `registerHandler(new MyCustomHandler())` to add more formats at runtime.
- Use the empty constructor if you want full control over which handlers are registered.

## Reading and Writing

```java
Path configPath = Path.of("config/flux-logging.yml");

// Deserialize YAML into the target type
LoggingConfigurationFile dto = io.read(configPath, LoggingConfigurationFile.class);

// Serialize a DTO back to disk
io.write(configPath, dto);
```

- `read(path, type)` infers the format from the file extension; `read(path, type, FileFormat.YAML)` bypasses inference.
- `write` auto-creates parent directories and, like `read`, can accept an explicit `FileFormat`.
- Handlers are responsible for the actual serialisation logic; they should throw `FileIOException` on recoverable errors.

## Creating Custom Handlers

```java
public final class JsonFileHandler implements FileHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public FileFormat getFormat() {
        return new FileFormat("json", Set.of("json"));
    }

    @Override
    public <T> T read(Path path, Class<T> targetType) {
        try {
            return mapper.readValue(path.toFile(), targetType);
        } catch (IOException exception) {
            throw new FileIOException("Failed to read JSON: " + path, exception);
        }
    }

    @Override
    public void write(Path path, Object data) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
        } catch (IOException exception) {
            throw new FileIOException("Failed to write JSON: " + path, exception);
        }
    }
}
```

Register the handler once during bootstrapping:

```java
FileIOManager io = new FileIOManager();
io.registerHandler(new YamlFileHandler());
io.registerHandler(new JsonFileHandler());
```

## DTO Mapping Strategy

- Prefer simple POJO/record DTOs that mirror the configuration schema (see `test-game`'s
  `game.core.logging.config.LoggingConfigurationFile` for a YAML example).
- Keep DTOs close to the consuming project so the schema can evolve without forcing changes to library consumers.
- After loading a DTO, translate it into the immutable runtime object (e.g., use `LoggingConfiguration.Builder`).

## Error Handling Tips

- Use meaningful messages when throwing `FileIOException` (include the path/format).
- Ensure handlers validate empty or null content and report config mistakes early.
- Consider wrapping optional configuration with defaults in DTO setters to keep runtime logic straightforward.

With these building blocks you can centralise IO concerns, reuse validation and future-proof the system for additional
formats (JSON, TOML, binary blobs, etc.) by adding new `FileHandler` implementations.

