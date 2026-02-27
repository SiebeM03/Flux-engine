package me.siebe.flux.core.system;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.List;

public class JavaInfoProvider implements SystemInfoProvider {
    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("Version", System.getProperty("java.version"));
        properties.put("Vendor", System.getProperty("java.vendor"));
        properties.put("JVM", System.getProperty("java.vm.name"));
        properties.put("JVM Vendor", System.getProperty("java.vm.vendor"));
        properties.put("JVM Version", System.getProperty("java.vm.version"));
        properties.put("JVM Args", getJvmArgs());
        return new StartupBannerSection("Java Runtime", properties);
    }

    private String getJvmArgs() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = runtimeBean.getInputArguments();
        if (!jvmArgs.isEmpty()) {
            return String.join(" ", jvmArgs);
        }
        return "";
    }
}
