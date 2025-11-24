package me.siebe.flux.util.logging.output;

import me.siebe.flux.util.logging.config.LoggingConfiguration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Writes log events to a file on disk.
 */
final class FileLogOutput implements LogOutput {
    private static final StandardOpenOption[] OPEN_OPTIONS = new StandardOpenOption[]{
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND
    };

    private final Path file;
    private final LogMessageFormatter formatter;
    private final Lock lock = new ReentrantLock();

    FileLogOutput(Path file, LogMessageFormatter formatter) {
        this.file = Objects.requireNonNull(file, "file");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    @Override
    public void write(LoggingConfiguration configuration, LogEvent event) {
        String rendered = formatter.format(configuration, event);

        lock.lock();
        try {
            ensureParentDirectory();
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, OPEN_OPTIONS)) {
                writer.write(rendered);
                writer.newLine();
                if (event.throwable() != null) {
                    writer.write(renderThrowable(event));
                }
            }
        } catch (IOException exception) {
            System.err.println("Failed to write log entry to '" + file + "': " + exception.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = file.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private String renderThrowable(LogEvent event) {
        StringWriter buffer = new StringWriter();
        try (PrintWriter printer = new PrintWriter(buffer)) {
            event.throwable().printStackTrace(printer);
        }
        return buffer.toString();
    }
}

