package me.siebe.flux.core.system;

import java.util.List;

public class SystemInfoService {
    private static final List<SystemInfoProvider> PROVIDERS = List.of(
            new FluxInfoProvider(),
            new OsInfoProvider(),
            new JavaInfoProvider(),
            new HeapInfoProvider(),
            new HardwareInfoProvider()
    );

    public static void populateStartupBanner() {
        for (SystemInfoProvider provider : PROVIDERS) {
            StartupBanner.addSection(provider.provide());
        }
    }
}
