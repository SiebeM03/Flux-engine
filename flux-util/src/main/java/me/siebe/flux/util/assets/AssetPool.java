package me.siebe.flux.util.assets;

import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;
import me.siebe.flux.util.logging.config.LoggingCategories;
import me.siebe.flux.util.memory.Copyable;

import java.util.HashMap;

public abstract class AssetPool<T> {
    private static final Logger logger = LoggerFactory.getLogger(AssetPool.class, LoggingCategories.ASSETS);

    final HashMap<String, T> assets;

    protected AssetPool() {
        assets = new HashMap<>();
    }

    public final T load(String identifier) {
        if (assets.containsKey(identifier)) {
            T asset = assets.get(identifier);
            if (shouldReturnClone()) {
                try {
                    if (asset instanceof Copyable<?> copyable) {
                        @SuppressWarnings("unchecked")
                        T assetClone = (T) copyable.copy();
                        return assetClone;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to clone asset {} with identifier {}, using original asset", asset, identifier);
                    return asset;
                }
            } else {
                return asset;
            }
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

    /**
     * Determines if the asset should be cloned when loaded from the pool.
     * Subclasses may override this method to return true if the asset should be cloned.
     * By default, the asset is not cloned.
     * @return true if the asset should be cloned, false otherwise
     */
    protected boolean shouldReturnClone() {
        return false;
    }
}
