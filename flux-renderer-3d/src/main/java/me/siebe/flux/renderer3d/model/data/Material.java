package me.siebe.flux.renderer3d.model.data;

import me.siebe.flux.lwjgl.opengl.texture.Texture;
import me.siebe.flux.util.FluxColor;
import me.siebe.flux.util.ValueUtils;
import org.joml.Vector3f;

public class Material {
    /**
     * Base color/albedo of the material. Used when no albedo texture is present.
     * Defaults to white.
     */
    private FluxColor baseColor = FluxColor.WHITE.copy();

    /**
     * Albedo/diffuse texture. Contains the base color information.
     * Can be null if using baseColor instead.
     */
    private Texture albedoTexture;

    /**
     * Normal map texture. Used for surface detail and bump mapping.
     * Can be null if no normal mapping is needed.
     */
    private Texture normalTexture;

    /**
     * Metallic-roughness texture. The R channel contains metallic value,
     * the G channel contains roughness value. Can be null if using scalar values instead.
     */
    private Texture metallicRoughnessTexture;

    /**
     * Metallic factor (0.0 = dielectric, 1.0 = metal).
     * Used when metallicRoughnessTexture is null or to scale the texture value.
     * Defaults to 0.0 (non-metallic).
     */
    private float metallicFactor = 0.0f;

    /**
     * Roughness factor (0.0 = smooth/mirror-like, 1.0 = rough).
     * Used when metallicRoughnessTexture is null or to scale the texture value.
     * Defaults to 0.5.
     */
    private float roughnessFactor = 0.5f;

    /**
     * Emissive texture. Used for materials that emit light.
     * Can be null if no emission is needed.
     */
    private Texture emissiveTexture;

    /**
     * Emissive color factor. Multiplies the emissive texture.
     * Defaults to black (no emission).
     */
    private Vector3f emissiveFactor = new Vector3f(0.0f, 0.0f, 0.0f);

    /**
     * Occlusion/ambient occlusion texture. Used for shadowing details.
     * Can be null if no occlusion mapping is needed.
     */
    private Texture occlusionTexture;

    /**
     * Occlusion strength factor. Controls the intensity of the occlusion effect.
     * Defaults to 1.0.
     */
    private float occlusionStrength = 1.0f;

    /**
     * Alpha cutoff value for alpha testing. Pixels with alpha below this value are discarded.
     * Only used when alphaMode is MASK. Defaults to 0.5.
     */
    private float alphaCutoff = 0.5f;

    /**
     * Alpha blending mode.
     */
    public enum AlphaMode {
        /**
         * Opaque material. Alpha channel is ignored.
         */
        OPAQUE,
        /**
         * Alpha testing. Pixels with alpha below alphaCutoff are discarded.
         */
        MASK,
        /**
         * Alpha blending. Pixels are blended based on alpha channel.
         */
        BLEND
    }

    /**
     * Alpha blending mode for this material.
     * Defaults to OPAQUE.
     */
    private AlphaMode alphaMode = AlphaMode.OPAQUE;

    /**
     * Whether the material is double-sided (rendered on both front and back faces).
     * Defaults to false.
     */
    private boolean doubleSided = false;


    /**
     * Creates a new material with default properties (white base color, non-metallic, medium roughness).
     */
    public Material() {
    }

    /**
     * Creates a new material with the specified base color, uses {@link FluxColor#WHITE} as fallback if the given value is null.
     * <p>
     * Always stores a copy of the passed value instead of a reference.
     *
     * @param baseColor the base color of the material
     */
    public Material(FluxColor baseColor) {
        this.baseColor = ValueUtils.valueWithFallback(baseColor, () -> FluxColor.WHITE).copy();
    }

    /**
     * Gets the base color of the material.
     *
     * @return the base color
     */
    public FluxColor getBaseColor() {
        return baseColor;
    }

    /**
     * Sets the base color of the material, uses {@link FluxColor#WHITE} as fallback if the given value is null.
     * <p>
     * Always stores a copy of the passed value instead of a reference.
     *
     * @param baseColor the base color to set
     */
    public void setBaseColor(FluxColor baseColor) {
        this.baseColor = ValueUtils.valueWithFallback(baseColor, () -> FluxColor.WHITE).copy();
    }

    /**
     * Gets the albedo texture.
     *
     * @return the albedo texture, or null if not set
     */
    public Texture getAlbedoTexture() {
        return albedoTexture;
    }

    /**
     * Sets the albedo texture.
     *
     * @param albedoTexture the albedo texture to set, can be null
     */
    public void setAlbedoTexture(Texture albedoTexture) {
        this.albedoTexture = albedoTexture;
    }

    /**
     * Gets the normal texture.
     *
     * @return the normal texture, or null if not set
     */
    public Texture getNormalTexture() {
        return normalTexture;
    }

    /**
     * Sets the normal texture.
     *
     * @param normalTexture the normal texture to set, can be null
     */
    public void setNormalTexture(Texture normalTexture) {
        this.normalTexture = normalTexture;
    }

    /**
     * Gets the metallic-roughness texture.
     *
     * @return the metallic-roughness texture, or null if not set
     */
    public Texture getMetallicRoughnessTexture() {
        return metallicRoughnessTexture;
    }

    /**
     * Sets the metallic-roughness texture.
     *
     * @param metallicRoughnessTexture the metallic-roughness texture to set, can be null
     */
    public void setMetallicRoughnessTexture(Texture metallicRoughnessTexture) {
        this.metallicRoughnessTexture = metallicRoughnessTexture;
    }

    /**
     * Gets the metallic factor.
     *
     * @return the metallic factor (0.0 to 1.0)
     */
    public float getMetallicFactor() {
        return metallicFactor;
    }


    /**
     * Sets the metallic factor.
     *
     * @param metallicFactor the metallic factor (0.0 to 1.0)
     */
    public void setMetallicFactor(float metallicFactor) {
        this.metallicFactor = ValueUtils.clampedValue(metallicFactor, 0.0f, 1.0f);
    }

    /**
     * Gets the roughness factor.
     *
     * @return the roughness factor (0.0 to 1.0)
     */
    public float getRoughnessFactor() {
        return roughnessFactor;
    }

    /**
     * Sets the roughness factor.
     *
     * @param roughnessFactor the roughness factor (0.0 to 1.0)
     */
    public void setRoughnessFactor(float roughnessFactor) {
        this.roughnessFactor = ValueUtils.clampedValue(roughnessFactor, 0.0f, 1.0f);
    }

    /**
     * Gets the emissive texture.
     *
     * @return the emissive texture, or null if not set
     */
    public Texture getEmissiveTexture() {
        return emissiveTexture;
    }

    /**
     * Sets the emissive texture.
     *
     * @param emissiveTexture the emissive texture to set, can be null
     */
    public void setEmissiveTexture(Texture emissiveTexture) {
        this.emissiveTexture = emissiveTexture;
    }

    /**
     * Gets the emissive factor.
     *
     * @return the emissive factor
     */
    public Vector3f getEmissiveFactor() {
        return emissiveFactor;
    }

    /**
     * Sets the emissive factor.
     *
     * @param emissiveFactor the emissive factor to set
     */
    public void setEmissiveFactor(Vector3f emissiveFactor) {
        this.emissiveFactor = ValueUtils.valueWithFallback(emissiveFactor, () -> new Vector3f(0.0f, 0.0f, 0.0f));
    }

    /**
     * Gets the occlusion texture.
     *
     * @return the occlusion texture, or null if not set
     */
    public Texture getOcclusionTexture() {
        return occlusionTexture;
    }

    /**
     * Sets the occlusion texture.
     *
     * @param occlusionTexture the occlusion texture to set, can be null
     */
    public void setOcclusionTexture(Texture occlusionTexture) {
        this.occlusionTexture = occlusionTexture;
    }

    /**
     * Gets the occlusion strength.
     *
     * @return the occlusion strength
     */
    public float getOcclusionStrength() {
        return occlusionStrength;
    }

    /**
     * Sets the occlusion strength.
     *
     * @param occlusionStrength the occlusion strength to set
     */
    public void setOcclusionStrength(float occlusionStrength) {
        this.occlusionStrength = ValueUtils.bottomClamped(occlusionStrength, 0.0f);
    }

    /**
     * Gets the alpha cutoff value.
     *
     * @return the alpha cutoff value
     */
    public float getAlphaCutoff() {
        return alphaCutoff;
    }

    /**
     * Sets the alpha cutoff value.
     *
     * @param alphaCutoff the alpha cutoff value to set
     */
    public void setAlphaCutoff(float alphaCutoff) {
        this.alphaCutoff = ValueUtils.clampedValue(alphaCutoff, 0.0f, 1.0f);
    }

    /**
     * Gets the alpha mode.
     *
     * @return the alpha mode
     */
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    /**
     * Sets the alpha mode.
     *
     * @param alphaMode the alpha mode to set
     */
    public void setAlphaMode(AlphaMode alphaMode) {
        this.alphaMode = ValueUtils.valueWithFallback(alphaMode, () -> AlphaMode.OPAQUE);
    }

    /**
     * Checks if the material is double-sided.
     *
     * @return true if double-sided, false otherwise
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    /**
     * Sets whether the material is double-sided.
     *
     * @param doubleSided true to enable double-sided rendering, false otherwise
     */
    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }
}
