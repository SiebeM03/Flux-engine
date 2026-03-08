package me.siebe.flux.test.junit;

import me.siebe.flux.core.system.StartupBanner;
import me.siebe.flux.core.system.StartupBannerSection;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;

public class StartupBannerMockExtension implements BeforeAllCallback, AfterAllCallback {
    private MockedStatic<StartupBanner> mock;

    @Override
    public void beforeAll(ExtensionContext context) {
        mock = Mockito.mockStatic(StartupBanner.class);
        mock.when(() -> StartupBanner.getSection(Mockito.anyString()))
                .thenAnswer(i -> new StartupBannerSection("", new HashMap<>()));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        mock.close();
    }
}
