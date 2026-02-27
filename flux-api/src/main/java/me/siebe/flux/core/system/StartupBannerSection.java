package me.siebe.flux.core.system;

import java.util.Map;

public record StartupBannerSection(
        String title,
        Map<String, String> properties
) {
}
