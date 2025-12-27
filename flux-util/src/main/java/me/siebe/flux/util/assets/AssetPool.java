package me.siebe.flux.util.assets;

import java.util.HashMap;

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

    protected abstract T create(String identifier);
}
