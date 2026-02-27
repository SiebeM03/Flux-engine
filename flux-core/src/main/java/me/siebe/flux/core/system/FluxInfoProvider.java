package me.siebe.flux.core.system;

import me.siebe.flux.core.ApplicationProperties;

import java.util.LinkedHashMap;

public class FluxInfoProvider implements SystemInfoProvider {
    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("Version", ApplicationProperties.getProperty(ApplicationProperties.ENGINE_VERSION));
        properties.put("LWJGL", ApplicationProperties.getProperty(ApplicationProperties.LWJGL_VERSION));
        return new StartupBannerSection("Flux", properties);
    }
}
