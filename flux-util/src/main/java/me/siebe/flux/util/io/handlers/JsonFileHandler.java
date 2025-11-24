package me.siebe.flux.util.io.handlers;

import me.siebe.flux.util.io.FileFormat;
import me.siebe.flux.util.io.FileHandler;
import me.siebe.flux.util.io.FileIOException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Objects;

/**
 * {@link FileHandler} implementation that serialises and deserialises data using the JSON file format.
 * <p>
 * This handler uses a minimal JSON parser designed for structured data. It supports reading JSON objects
 * as {@link Map} instances and can write any object that can be serialized to JSON.
 */
public class JsonFileHandler implements FileHandler {

    /**
     * Creates a new JSON file handler.
     */
    public JsonFileHandler() {
    }

    @Override
    public FileFormat getFormat() {
        return FileFormat.JSON;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Path path, Class<T> targetType) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(targetType, "targetType");

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String json = reader.lines().reduce("", (a, b) -> a + b);
            return parse(json, targetType);
        } catch (IOException exception) {
            throw new FileIOException("Failed to read JSON file: " + path, exception);
        } catch (IllegalArgumentException exception) {
            throw new FileIOException("Failed to parse JSON content from: " + path, exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(String content, Class<T> targetType) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(targetType, "targetType");

        Object result = parseInternal(content);

        if (result == null) {
            throw new FileIOException("JSON content is empty or contains null content.");
        }

        // Handle Map<String, Object> as a special case for dynamic JSON structures
        if (targetType == Map.class || targetType == Object.class) {
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                T mapResult = (T) result;
                return mapResult;
            }
            throw new FileIOException("JSON content does not contain a JSON object (expected Map, got " + result.getClass().getSimpleName() + ")");
        }

        // For other types, we'd need a more sophisticated deserializer
        // For now, we only support Map<String, Object>
        throw new FileIOException("JSON handler currently only supports reading as Map<String, Object>. Requested type: " + targetType.getName());
    }

    @Override
    public void write(Path path, Object data) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(data, "data");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            String json = serialize(data);
            writer.write(json);
        } catch (IOException exception) {
            throw new FileIOException("Failed to write JSON file: " + path, exception);
        } catch (IllegalArgumentException exception) {
            throw new FileIOException("Failed to serialise JSON content for: " + path, exception);
        }
    }

    /**
     * Parses a JSON string into a map of key-value pairs.
     * <p>
     * This is a convenience method that calls {@link #parse(String, Class)} with {@link Map} as the target type.
     *
     * @param json the JSON string
     * @return parsed JSON object as a map
     * @throws IllegalArgumentException if the JSON is invalid
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parse(String json) {
        return parse(json, Map.class);
    }

    /**
     * Internal parsing method that returns the raw parsed object.
     *
     * @param content the JSON string
     * @return parsed object (may be Map, List, String, Number, Boolean, or null)
     * @throws IllegalArgumentException if the JSON is invalid
     */
    private Object parseInternal(String content) {
        JsonTokenizer tokenizer = new JsonTokenizer(content);
        return parseValue(tokenizer);
    }

    /**
     * Parses a JSON value (object, array, string, number, boolean, null).
     */
    private Object parseValue(JsonTokenizer tokenizer) {
        tokenizer.skipWhitespace();
        char c = tokenizer.peek();
        if (c == '{') {
            return parseObject(tokenizer);
        } else if (c == '[') {
            return parseArray(tokenizer);
        } else if (c == '"') {
            return parseString(tokenizer);
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            return parseNumber(tokenizer);
        } else if (c == 't' || c == 'f') {
            return parseBoolean(tokenizer);
        } else if (c == 'n') {
            return parseNull(tokenizer);
        } else {
            throw new IllegalArgumentException("Unexpected character: " + c);
        }
    }

    /**
     * Parses a JSON object.
     */
    private Map<String, Object> parseObject(JsonTokenizer tokenizer) {
        Map<String, Object> obj = new LinkedHashMap<>();
        tokenizer.expect('{');
        tokenizer.skipWhitespace();

        if (tokenizer.peek() == '}') {
            tokenizer.advance();
            return obj;
        }

        while (true) {
            tokenizer.skipWhitespace();
            String key = parseString(tokenizer);
            tokenizer.skipWhitespace();
            tokenizer.expect(':');
            tokenizer.skipWhitespace();
            Object value = parseValue(tokenizer);
            obj.put(key, value);
            tokenizer.skipWhitespace();

            char c = tokenizer.peek();
            if (c == '}') {
                tokenizer.advance();
                break;
            } else if (c == ',') {
                tokenizer.advance();
            } else {
                throw new IllegalArgumentException("Expected ',' or '}', got: " + c);
            }
        }

        return obj;
    }

    /**
     * Parses a JSON array.
     */
    private List<Object> parseArray(JsonTokenizer tokenizer) {
        List<Object> arr = new ArrayList<>();
        tokenizer.expect('[');
        tokenizer.skipWhitespace();

        if (tokenizer.peek() == ']') {
            tokenizer.advance();
            return arr;
        }

        while (true) {
            tokenizer.skipWhitespace();
            Object value = parseValue(tokenizer);
            arr.add(value);
            tokenizer.skipWhitespace();

            char c = tokenizer.peek();
            if (c == ']') {
                tokenizer.advance();
                break;
            } else if (c == ',') {
                tokenizer.advance();
            } else {
                throw new IllegalArgumentException("Expected ',' or ']', got: " + c);
            }
        }

        return arr;
    }

    /**
     * Parses a JSON string.
     */
    private String parseString(JsonTokenizer tokenizer) {
        tokenizer.expect('"');
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMore()) {
            char c = tokenizer.peek();
            if (c == '"') {
                tokenizer.advance();
                break;
            } else if (c == '\\') {
                tokenizer.advance();
                char escaped = tokenizer.peek();
                switch (escaped) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        tokenizer.advance();
                        String hex = tokenizer.read(4);
                        sb.append((char) Integer.parseInt(hex, 16));
                    }
                    default -> throw new IllegalArgumentException("Invalid escape sequence: \\" + escaped);
                }
                tokenizer.advance();
            } else {
                sb.append(c);
                tokenizer.advance();
            }
        }
        return sb.toString();
    }

    /**
     * Parses a JSON number.
     */
    private Number parseNumber(JsonTokenizer tokenizer) {
        StringBuilder sb = new StringBuilder();
        if (tokenizer.peek() == '-') {
            sb.append('-');
            tokenizer.advance();
        }

        while (tokenizer.hasMore() && Character.isDigit(tokenizer.peek())) {
            sb.append(tokenizer.peek());
            tokenizer.advance();
        }

        if (tokenizer.hasMore() && tokenizer.peek() == '.') {
            sb.append('.');
            tokenizer.advance();
            while (tokenizer.hasMore() && Character.isDigit(tokenizer.peek())) {
                sb.append(tokenizer.peek());
                tokenizer.advance();
            }
        }

        if (tokenizer.hasMore() && (tokenizer.peek() == 'e' || tokenizer.peek() == 'E')) {
            sb.append(tokenizer.peek());
            tokenizer.advance();
            if (tokenizer.peek() == '+' || tokenizer.peek() == '-') {
                sb.append(tokenizer.peek());
                tokenizer.advance();
            }
            while (tokenizer.hasMore() && Character.isDigit(tokenizer.peek())) {
                sb.append(tokenizer.peek());
                tokenizer.advance();
            }
        }

        String numStr = sb.toString();
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.parseDouble(numStr);
        } else {
            long value = Long.parseLong(numStr);
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            return value;
        }
    }

    /**
     * Parses a JSON boolean.
     */
    private Boolean parseBoolean(JsonTokenizer tokenizer) {
        if (tokenizer.peek() == 't') {
            tokenizer.expect("true");
            return true;
        } else {
            tokenizer.expect("false");
            return false;
        }
    }

    /**
     * Parses JSON null.
     */
    private Object parseNull(JsonTokenizer tokenizer) {
        tokenizer.expect("null");
        return null;
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param data object to serialize
     * @return JSON string representation
     */
    private String serialize(Object data) {
        if (data == null) {
            return "null";
        } else if (data instanceof Map) {
            return serializeMap((Map<?, ?>) data);
        } else if (data instanceof List) {
            return serializeList((List<?>) data);
        } else if (data instanceof String) {
            return serializeString((String) data);
        } else if (data instanceof Number || data instanceof Boolean) {
            return data.toString();
        } else {
            // For other types, serialize as string (could be enhanced later)
            return serializeString(data.toString());
        }
    }

    private String serializeMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(serializeString(entry.getKey().toString()));
            sb.append(":");
            sb.append(serialize(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeList(List<?> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(serialize(item));
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeString(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Simple tokenizer for JSON parsing.
     */
    private static class JsonTokenizer {
        private final String json;
        private int position;

        JsonTokenizer(String json) {
            this.json = json;
            this.position = 0;
        }

        boolean hasMore() {
            return position < json.length();
        }

        char peek() {
            if (!hasMore()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            return json.charAt(position);
        }

        void advance() {
            position++;
        }

        void expect(char expected) {
            char actual = peek();
            if (actual != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "', got '" + actual + "'");
            }
            advance();
        }

        void expect(String expected) {
            for (int i = 0; i < expected.length(); i++) {
                expect(expected.charAt(i));
            }
        }

        String read(int count) {
            if (position + count > json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            String result = json.substring(position, position + count);
            position += count;
            return result;
        }

        void skipWhitespace() {
            while (hasMore() && Character.isWhitespace(peek())) {
                advance();
            }
        }
    }
}

