package me.siebe.flux.util.logging;

import java.util.Objects;

/**
 * Provides lightweight SLF4J-style message formatting where {@code {}} placeholders are replaced by
 * the supplied arguments in order. A backslash ({@code \}) can be used to escape braces.
 */
final class MessageFormatter {
    private static final char ESCAPE = '\\';
    private static final char OPEN = '{';
    private static final char CLOSE = '}';

    private MessageFormatter() {
    }

    static String format(String message, Object... arguments) {
        if (message == null) {
            return "null";
        }
        if (arguments == null || arguments.length == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder(message.length() + 16 * arguments.length);
        int argIndex = 0;

        for (int index = 0; index < message.length(); index++) {
            char current = message.charAt(index);

            if (current == ESCAPE && index + 1 < message.length()) {
                char next = message.charAt(index + 1);
                if (next == OPEN || next == CLOSE) {
                    builder.append(next);
                    index++;
                    continue;
                }
                builder.append(current);
                continue;
            }

            if (current == OPEN && index + 1 < message.length() && message.charAt(index + 1) == CLOSE) {
                if (argIndex < arguments.length) {
                    builder.append(Objects.toString(arguments[argIndex], "null"));
                    argIndex++;
                } else {
                    builder.append(OPEN).append(CLOSE);
                }
                index++;
                continue;
            }

            builder.append(current);
        }

        return builder.toString();
    }
}

