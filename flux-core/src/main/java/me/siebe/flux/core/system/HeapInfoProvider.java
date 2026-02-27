package me.siebe.flux.core.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashMap;

public class HeapInfoProvider implements SystemInfoProvider {
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("Max", getMaxMemory());
        properties.put("Used", getUsedMemory());
        properties.put("Committed", getCommittedMemory());
        return new StartupBannerSection("Heap Memory", properties);
    }

    private String getMaxMemory() {
        long maxHeapMemory = memoryMXBean.getHeapMemoryUsage().getMax() / (1024 * 1024);    // MB
        return maxHeapMemory + " MB";
    }

    private String getUsedMemory() {
        long usedHeapMemory = memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        return usedHeapMemory + " MB";
    }

    private String getCommittedMemory() {
        long committedHeapMemory = memoryMXBean.getHeapMemoryUsage().getCommitted() / (1024 * 1024);
        return committedHeapMemory + " MB";
    }
}
