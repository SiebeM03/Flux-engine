package me.siebe.flux.ui;

import me.siebe.flux.util.system.ProvidableSystem;
import me.siebe.flux.util.system.SystemProvider;
import me.siebe.flux.util.system.SystemProviderType;

import java.util.Queue;

public interface Ui extends ProvidableSystem {
    UIScene createScene();

    Queue<UIScene> getScenes();

    static Ui findImplementation() {
        return SystemProvider.provide(Ui.class, SystemProviderType.ALL);
    }
}
