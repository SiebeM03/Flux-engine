package me.siebe.flux.core.system;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.AnsiColor;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.util.LinkedHashMap;
import java.util.Map;

public class StartupBanner {
    private static final Logger logger = LoggerFactory.getLogger(StartupBanner.class, LoggingCategories.ENGINE);

    private static final Map<String, StartupBannerSection> sections = new LinkedHashMap<>();

    public static void addSection(StartupBannerSection section) {
        sections.put(section.title(), section);
    }

    public static StartupBannerSection getSection(String title) {
        return sections.get(title);
    }

    public static void render() {
        StringBuilder builder = new StringBuilder();

        addLogo(builder);

        for (StartupBannerSection section : sections.values()) {
            addHeaderToBuilder(builder, section.title());
            for (Map.Entry<String, String> entry : section.properties().entrySet()) {
                addDataRowToBuilder(builder, entry.getKey(), entry.getValue());
            }
        }

        builder.append(AnsiColor.RESET.code()).append(System.lineSeparator());

        for (String line : builder.toString().split(System.lineSeparator())) {
            logger.info(line);
        }
    }

    private static void addLogo(StringBuilder builder) {
        // https://patorjk.com/software/taag font Speed / Doom / Slant
        String logo = """

                _______________                          \s
                ___  ____/__  /___  _____  __            \s
                __  /_   __  /_  / / /_  |/_/            \s
                _  __/   _  / / /_/ /__>  <              \s
                /_/      /_/  \\__,_/ /_/|_|              \s

                __________              _____            \s
                ___  ____/_____________ ___(_)___________\s
                __  __/  __  __ \\_  __ `/_  /__  __ \\  _ \\
                _  /___  _  / / /  /_/ /_  / _  / / /  __/
                /_____/  /_/ /_/_\\__, / /_/  /_/ /_/\\___/\s
                                /____/                   \s
                """;

        for (String line : logo.split(System.lineSeparator())) {
            addLineToBuilder(builder, line, AnsiColor.BRIGHT_CYAN);
        }
    }

    private static void addLineToBuilder(StringBuilder builder, String text, AnsiColor color) {
        builder
                .append(color.code())
                .append(text)
                .append(AnsiColor.RESET.code())
                .append(System.lineSeparator());
    }

    private static void addHeaderToBuilder(StringBuilder builder, String header) {
        int maxLineLength = 72;
        String prefix = "┌─ ";
        String suffixChar = "─";

        String headerStart = prefix + header + " ";
        String headerEnd = suffixChar.repeat(maxLineLength - Math.min(maxLineLength, headerStart.length()));

        builder
                .append(System.lineSeparator())
                .append(AnsiColor.BRIGHT_YELLOW.code())
                .append(headerStart)
                .append(headerEnd)
                .append(AnsiColor.RESET.code())
                .append(System.lineSeparator());
    }

    private static void addDataRowToBuilder(StringBuilder builder, String label, String value) {
        int valueOffset = 20;
        String spacesAfterLabel = " ".repeat(valueOffset - Math.min(valueOffset, label.length()));

        if (value == null || value.isEmpty()) {
            value = "Unknown";
        }
        builder
                .append("\t")
                .append(AnsiColor.BRIGHT_WHITE.code())
                .append(label)
                .append(":")
                .append(spacesAfterLabel)
                .append(AnsiColor.RESET.code())
                .append(AnsiColor.CYAN.code())
                .append(value)
                .append(AnsiColor.RESET.code())
                .append(System.lineSeparator());
    }

}
