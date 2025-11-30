package me.siebe.flux.core;


import me.siebe.flux.util.exceptions.ApplicationException;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

import java.util.List;

public class FluxLauncher {
    public static void main(String[] args) {
        List<FluxApplication> providers = SystemProvider.provideAll(FluxApplication.class, SystemProviderType.CUSTOM_ONLY);
        if (providers.size() > 1) {
            throw ApplicationException.multipleAppProviderImplementationFound();
        }

        // This will retrieve the application implementation in the game project
        FluxApplication app = providers.stream().findFirst()
                .orElseThrow(ApplicationException::noAppProviderImplementationFound);

        // Store the application instance in AppContext
        AppContext.get().setApplication(app);
        // Application lifecycle
        app.init();
        app.run();
        app.destroy();
    }
}
