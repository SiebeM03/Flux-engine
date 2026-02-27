package me.siebe.flux.core.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedHashMap;

public class HardwareInfoProvider implements SystemInfoProvider {
    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("RAM Size", getTotalRamMemory());
        properties.put("CPU Cores", getCpuCores());
        return new StartupBannerSection("Hardware", properties);
    }

    private String getTotalRamMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            long totalMemory = sunOsBean.getTotalMemorySize() / (1024 * 1024);
            return totalMemory + " MB";
        }
        return "";
    }

    private String getCpuCores() {
        return Runtime.getRuntime().availableProcessors() + "";
    }
}
