package me.siebe.flux.util.string;

import java.util.Objects;

/**
 * Provides lightweight SLF4J-style message formatting where {@code {}} placeholders are replaced by
 * the supplied arguments in order. A backslash ({@code \}) can be used to escape braces.
 */
public final class MessageFormatter {
    private static final char ESCAPE = '\\';
    private static final char OPEN = '{';
    private static final char CLOSE = '}';

    private MessageFormatter() {
    }

    public static String format(String message, Object... arguments) {
        if (message == null) return "null";
        if (arguments == null || arguments.length == 0) return message;

        StringBuilder builder = new StringBuilder(message.length() + 16 * arguments.length);
        int argIndex = 0;

        for (int index = 0; index < message.length(); index++) {
            char current = message.charAt(index);

            if (isEscapeChar(current) && hasCharAfter(message, index)) {
                char next = message.charAt(index + 1);
                if (next == OPEN || next == CLOSE) {
                    builder.append(next);
                    index++;
                    continue;
                }
                builder.append(current);
                continue;
            }

            if (isPlaceHolderFormat(message, index)) {
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

    private static boolean isPlaceHolderFormat(String message, int index) {
        return isOpenChar(message.charAt(index))
                && hasCharAfter(message, index)
                && isCloseChar(message.charAt(index + 1));
    }

    private static boolean isEscapeChar(char c) {
        return c == ESCAPE;
    }

    private static boolean isOpenChar(char c) {
        return c == OPEN;
    }

    private static boolean isCloseChar(char c) {
        return c == CLOSE;
    }

    private static boolean hasCharAfter(String message, int index) {
        return index + 1 < message.length();
    }
}

