package me.siebe.flux.util.assets;

import java.util.HashMap;

public abstract class AssetPool<T> {
    private HashMap<String, T> assets;

    protected AssetPool() {
        assets = new HashMap<>();
    }

    public T load(String identifier) {
        // FIXME cannot render multiple models with the same key (only 1 instance is ever returned)
        //  either support OpenGL instancing or just return a whole new copy
        if (assets.containsKey(identifier)) {
            return assets.get(identifier);
        }
        T asset = create(identifier);
        assets.put(identifier, asset);
        return asset;
    }

    protected abstract T create(String identifier);
}
