package me.siebe.flux.core;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.LoggingManager;
import me.siebe.flux.util.logging.config.AnsiColor;
import me.siebe.flux.util.logging.config.LoggingCategories;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class StartupBanner {
    private static final Logger logger = LoggerFactory.getLogger(StartupBanner.class, LoggingCategories.ENGINE);

    public static void displayBanner() {
        if (!LoggingManager.configuration().isStartupBannerEnabled()) return;

        StringBuilder builder = new StringBuilder();

        addLogo(builder);

        if (LoggingManager.configuration().isStartupBannerLogSystemInfo()) {
            addApplicationInfo(builder);
            addOperatingSystemInfo(builder);
            addJavaInfo(builder);
            addHeapInfo(builder);
            addHardwareInfo(builder);
            addOpenGlInfo(builder);
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

    private static void addApplicationInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Application");
        addDataRowToBuilder(builder, "Version", "Flux Engine " + EngineInfo.getVersion());
        addDataRowToBuilder(builder, "LWJGL", EngineInfo.getLWJGLVersion());
    }

    private static void addOperatingSystemInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Operating System");
        addDataRowToBuilder(builder, "OS", OsInfo.getOSNameAndArch());
        addDataRowToBuilder(builder, "Hostname", OsInfo.getHostName());
    }

    private static void addJavaInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Java Runtime");
        addDataRowToBuilder(builder, "Version", JavaInfo.getVersion());
        addDataRowToBuilder(builder, "Vendor", JavaInfo.getVendor());
        addDataRowToBuilder(builder, "JVM", JvmInfo.getName());
        addDataRowToBuilder(builder, "JVM Vendor", JvmInfo.getVendor());
        addDataRowToBuilder(builder, "JVM Version", JvmInfo.getVersion());
        addDataRowToBuilder(builder, "JVM Args", JvmInfo.getArgs());
    }

    private static void addHeapInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Heap Memory");
        addDataRowToBuilder(builder, "Max", HeapInfo.getMaxMemory());
        addDataRowToBuilder(builder, "Used", HeapInfo.getUsedMemory());
        addDataRowToBuilder(builder, "Committed", HeapInfo.getCommittedMemory());
    }

    private static void addHardwareInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Hardware");
        addDataRowToBuilder(builder, "RAM Size", RamInfo.getTotalSystemMemory());
        addDataRowToBuilder(builder, "CPU Cores", CpuInfo.getCores());
        addDataRowToBuilder(builder, "GPU Name", GpuInfo.getGpuName());
    }

    private static void addOpenGlInfo(StringBuilder builder) {
        addHeaderToBuilder(builder, "Graphics");
        addDataRowToBuilder(builder, "OpenGL", OpenGlInfo.getOpenGLVersion());
        addDataRowToBuilder(builder, "GLSL", OpenGlInfo.getGlslVersion());
        addDataRowToBuilder(builder, "Max Texture Size", OpenGlInfo.getMaxTextureSize());
        addDataRowToBuilder(builder, "Max Texture Units", OpenGlInfo.getMaxTextureUnits());
        addDataRowToBuilder(builder, "Max Vertex Attribs", OpenGlInfo.getMaxVertexAttribs());
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


    private static class EngineInfo {
        private static String getVersion() {
            return fallback(() -> ApplicationProperties.getProperty(ApplicationProperties.ENGINE_VERSION));
        }

        private static String getLWJGLVersion() {
            return fallback(() -> ApplicationProperties.getProperty(ApplicationProperties.LWJGL_VERSION));
        }
    }

    private static class OsInfo {
        private static String getOSNameAndArch() {
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            if (osName == null) osName = "Unknown";
            if (osArch == null) osArch = "Unknown";
            return osName + " (" + osArch + ")";
        }

        private static String getHostName() {
            return fallback(() -> {
                try {
                    return InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static class JavaInfo {
        private static String getVersion() {
            return fallback(() -> System.getProperty("java.version"));
        }

        private static String getVendor() {
            return fallback(() -> System.getProperty("java.vendor"));
        }
    }

    private static class JvmInfo {
        private static String getName() {
            return fallback(() -> System.getProperty("java.vm.name"));
        }

        private static String getVersion() {
            return fallback(() -> System.getProperty("java.vm.version"));
        }

        private static String getVendor() {
            return fallback(() -> System.getProperty("java.vm.vendor"));
        }

        private static String getArgs() {
            return fallback(() -> {
                RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
                List<String> jvmArgs = runtimeBean.getInputArguments();
                if (!jvmArgs.isEmpty()) {
                    return String.join(" ", jvmArgs);
                }
                return null;
            });
        }
    }

    private static class HeapInfo {
        private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        private static String getMaxMemory() {
            return fallback(() -> {
                long maxHeapMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);  // MB
                return maxHeapMemory + " MB";
            });
        }

        private static String getUsedMemory() {
            return fallback(() -> {
                long usedHeapMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
                return usedHeapMemory + " MB";
            });
        }

        private static String getCommittedMemory() {
            return fallback(() -> {
                long committedHeapMemory = memoryBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024);
                return committedHeapMemory + " MB";
            });
        }
    }

    private static class RamInfo {
        private static String getTotalSystemMemory() {
            return fallback(() -> {
                OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                    long totalMemory = sunOsBean.getTotalMemorySize() / (1024 * 1024);
                    return totalMemory + " MB";
                }
                return null;
            });
        }
    }

    private static class CpuInfo {
        private static String getCores() {
            return fallback(() -> Runtime.getRuntime().availableProcessors() + "");
        }
    }

    private static class GpuInfo {
        private static String getGpuName() {
            return fallback(() -> glGetString(GL_RENDERER));
        }
    }

    private static class OpenGlInfo {
        private static String getOpenGLVersion() {
            return fallback(() -> glGetString(GL_VERSION));
        }

        private static String getGlslVersion() {
            return fallback(() -> glGetString(GL_SHADING_LANGUAGE_VERSION));
        }

        private static String getMaxTextureSize() {
            return fallback(() -> {
                int[] maxSize = new int[1];
                glGetIntegerv(GL_MAX_TEXTURE_SIZE, maxSize);
                return maxSize[0] + " x " + maxSize[0];
            });
        }

        private static String getMaxTextureUnits() {
            return fallback(() -> {
                int[] maxUnits = new int[1];
                glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maxUnits);
                return String.valueOf(maxUnits[0]);
            });
        }

        private static String getMaxVertexAttribs() {
            return fallback(() -> {
                int[] maxAttribs = new int[1];
                glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxAttribs);
                return String.valueOf(maxAttribs[0]);
            });
        }
    }


    private static String fallback(Supplier<String> valueSupplier) {
        try {
            String value = valueSupplier.get();
            if (value == null || value.isEmpty()) return "Unknown";
            return value;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
