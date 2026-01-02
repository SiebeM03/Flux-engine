package me.siebe.flux.util.assets;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class AssetPool<T> {
    private HashMap<String, T> assets;

    protected AssetPool() {
        assets = new HashMap<>();
    }

    public T load(String identifier) {
        if (assets.containsKey(identifier)) {
            return assets.get(identifier);
        }
        T asset = create(identifier);
        assets.put(identifier, asset);
        return asset;
    }

    public T load(String identifier, Supplier<T> supplier) {
        if (assets.containsKey(identifier)) {
            return assets.get(identifier);
        }
        T asset = supplier.get();
        assets.put(identifier, asset);
        return asset;
    }

    protected abstract T create(String identifier);

    public void add(String identifier, T asset) {
        assets.put(identifier, asset);
    }
}
