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

    /**
     * Puts an asset into the pool under the given identifier.
     * Subclasses may use this after creating a new instance during hot-reload.
     *
     * @param identifier the asset identifier
     * @param asset      the asset to store
     */
    protected final void putAsset(String identifier, T asset) {
        assets.put(identifier, asset);
    }

    /**
     * Removes an asset from the pool without destroying it.
     * Subclasses may use this for hot-reload: remove, destroy the old instance, then put a new one.
     *
     * @param identifier the asset identifier
     * @return the removed asset, or null if not present
     */
    protected final T removeAsset(String identifier) {
        return assets.remove(identifier);
    }

    /**
     * Replaces an asset from the pool without destroying it. Subclasses may use this for hot-reload.
     *
     * @param identifier the asset identifier
     * @param asset      the new asset to store
     * @return the removed asset, or null if not present
     */
    protected final T replaceAsset(String identifier, T asset) {
        return assets.put(identifier, asset);
    }

}
