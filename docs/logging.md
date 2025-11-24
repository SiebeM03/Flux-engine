# Flux Logging System

The Flux logging subsystem provides a lightweight, configurable alternative to SLF4J that supports log
levels, categories, and ANSI colour formatting. This document explains the core concepts and shows how to
use and customise the system within engine modules and games.

## Core Types

- `Logger`: Entry point for emitting log messages. The API mirrors the familiar SLF4J style with helper
  methods like `info`, `warn`, and `error`.
- `LoggerFactory`: Retrieves or creates `Logger` instances. Use the class-based factory methods to keep the
  code concise.
- `LoggingManager`: Stores and mutates global logging configuration, performs level checks, formats
  messages, and dispatches them to the configured outputs (console, file, …).
- `LoggingConfiguration`: Immutable configuration object that controls minimum log levels per category,
  colour usage, timestamp formatting, and output targets. Instances are produced via its builder.
- `LoggingCategories`: Interface that defines the built-in categories (`general`, `engine`, `renderer`,
  `event`, `window`). You can extend this interface to add project-specific categories.
- `LogLevel`: Enumeration of the supported log levels (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`) ordered
  by severity. Each level has a default ANSI colour.
- `AnsiColor`: Enumeration of ANSI escape codes used for colourising console output.

## Obtaining a Logger

```java
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.LoggingCategories;

private static final Logger LOGGER =
        LoggerFactory.getLogger(MyRenderer.class, LoggingCategories.RENDERER);
```

- `LoggerFactory.getLogger(Class<?>)` attaches the logger to the `general` category.
- `LoggerFactory.getLogger(Class<?>, String)` lets you choose a category. The factory caches loggers per
  `(category, name)` pair so you can freely reuse the call.
- Categories are normalised to lower-case and default to `general` if they are blank or `null`.

## Emitting Messages

```java
LOGGER.info("Initialised renderer in {} ms", initTimeMillis);
LOGGER.warn("Fallback shader in use for {}", materialName);
LOGGER.error("Failed to load texture {}", texturePath, throwable);
```

- SLF4J-style `{}` placeholders are supported. Arguments are converted using `Objects.toString`.
- The overloads that accept a `Throwable` print the stack trace on the appropriate stream (stderr for
  `ERROR` or higher; stdout otherwise).
- `Logger.isDebugEnabled()` (and similar methods) allows guarding expensive log message creation.

## Configuring Output

The `LoggingManager` exposes several static helpers to manage configuration at runtime:

```java
import me.siebe.flux.util.logging.LoggingManager;
import me.siebe.flux.util.logging.LoggingConfiguration;
import me.siebe.flux.util.logging.LogLevel;
import me.siebe.flux.util.logging.AnsiColor;
import me.siebe.flux.util.logging.LoggingCategories;

// Replace the configuration entirely
LoggingConfiguration configuration = LoggingConfiguration.builder()
        .defaultLevel(LogLevel.INFO)
        .categoryLevel(LoggingCategories.RENDERER, LogLevel.DEBUG)
        .categoryLevel("physics", LogLevel.TRACE)
        .levelColor(LogLevel.ERROR, AnsiColor.BRIGHT_RED)
        .colorEnabled(true)
        .clearOutputs()
        .addOutput(LoggingOutputConfiguration.console().pattern("[{level}] {message}").build())
        .addOutput(LoggingOutputConfiguration.fileOutput()
                .file(Path.of("logs/engine.log"))
                .pattern("[{time}] [{level}] [{category}] {logger}: {message}")
                .build())
        .build();
LoggingManager.
        configure(configuration);

// Adjust specific aspects without rebuilding from scratch
LoggingManager.
        setCategoryLevel(LoggingCategories.EVENT, LogLevel.WARN);
LoggingManager.
        clearCategoryLevel("physics");
LoggingManager.
        setLevelColor(LogLevel.DEBUG, AnsiColor.BRIGHT_CYAN);
LoggingManager.
        setColorEnabled(false);
```

### Configuration via YAML

By default the engine looks for a `flux-logging.yml` resource on the classpath (either in the root or under
`config/`). When present, it is loaded through `LoggingConfigLoader.loadInitialConfiguration()` during engine
startup and becomes the active configuration. If no resource is found, the default configuration (INFO level,
console output) is used.

Example file from `/src/main/resources/config/flux-logging.yml`:

```yaml
defaultLevel: INFO
categories:
  engine: DEBUG
  game: INFO
  physics: WARN

outputs:
  - type: console
    pattern: "[{level}] [{category}] {message}"
  - type: file
    file: "logs/flux.log"
    pattern: "[{time}] [{level}] [{category}] {logger} :: {message}"
```

#### YAML fields

- `defaultLevel` (string, optional): fallback level when a category has no override. Defaults to `INFO`.
- `colorEnabled` (boolean, optional): toggles ANSI colour output. Defaults to `true`.
- `timestampFormat` (string, optional): `DateTimeFormatter` pattern for `{time}` tokens.
- `categories` (map<string, string>, optional): per-category minimum levels. Keys are normalised to lowercase.
- `levelColors` (map<string, string>, optional): overrides the colour used for a level (expects `AnsiColor` names).
- `outputs` (list, optional): ordered list of output definitions. When omitted the console output is configured automatically.
  - `type` (`console` | `file`, required): destination type.
  - `pattern` (string, optional): format pattern. Defaults to `[{time}] [{level}] [{category}] {message}`.
  - `file` (string, required for `file` outputs): path to append log entries to; directories are created on demand.

Patterns support the following tokens: `{time}`, `{level}`, `{category}`, `{logger}`, `{message}`. Unknown tokens are
left untouched so you can mix literals freely.

#### Runtime adjustments

- `LoggingManager.configure(Consumer<LoggingConfiguration.Builder>)` allows incremental updates based on
  the current configuration.
- Category identifiers are normalised in the same way as logger creation, so `physics`, `PHYSICS`, and
  `Physics` all refer to the same category.
- File outputs acquire a lock per configured file, making concurrent writes safe without blocking console emission.

## Extending Categories

`LoggingCategories` is declared as an interface, which makes it simple to extend the set of shared names:

```java
public interface MyCategories extends LoggingCategories {
    String PHYSICS = "physics";
    String AUDIO = "audio";
}
```

You can then reference these constants anywhere you obtain a logger or configure the system.

## Colour Control

- Colours default to the scheme defined in `LogLevel`.
- When colours are enabled, messages are wrapped with the configured ANSI escape codes and automatically
  reset afterwards.
- Disabling colours leaves the message text untouched; the level and category information are still
  included.

## Thread Safety

- Logger instances are immutable and therefore thread-safe.
- `LoggingManager` keeps the active `LoggingConfiguration` in an `AtomicReference` and its internal caches
  in concurrent maps, so updates and reads can occur from any thread safely.
- Configuration updates replace the entire configuration atomically.

## Best Practices

- Create one `Logger` per class and keep it in a static field to avoid repeated lookups.
- Use categories to group related systems and adjust their verbosity independently.
- Guard verbose or expensive log statements with the relevant `isXEnabled()` checks.
- Apply configuration once during engine initialisation so both the engine and user code share the same
  logging behaviour.

