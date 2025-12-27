#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in mat3 TBN;

out vec4 FragColor;

/**
 * Material properties structure.
 * Contains all material parameters for rendering.
 */
struct Material {
    vec4 baseColor;// Base color/albedo (RGBA)
    float metallicFactor;// Metallic factor (0.0 = dielectric, 1.0 = metal)
    float roughnessFactor;// Roughness factor (0.0 = smooth, 1.0 = rough)
    vec3 emissiveFactor;// Emissive color factor
    float occlusionStrength;// Occlusion strength factor
    float alphaCutoff;// Alpha cutoff for MASK mode
    int alphaMode;// Alpha mode: 0=OPAQUE, 1=MASK, 2=BLEND
};

/**
 * Material textures structure.
 * Contains all texture samplers used by the material.
 */
struct MaterialTextures {
    sampler2D albedo;// Albedo/diffuse texture
    sampler2D normal;// Normal map texture
    sampler2D metallicRoughness;// Metallic-roughness texture (R=metallic, G=roughness)
    sampler2D emissive;// Emissive texture
    sampler2D occlusion;// Occlusion/ambient occlusion texture
};

/**
 * Material texture flags structure.
 * Indicates which textures are present and should be sampled.
 * Values are 0 (false) or 1 (true).
 */
struct MaterialFlags {
    int hasAlbedoTexture;
    int hasNormalTexture;
    int hasMetallicRoughnessTexture;
    int hasEmissiveTexture;
    int hasOcclusionTexture;
};

// Material uniform
uniform Material uMaterial;

// Material textures uniform
uniform MaterialTextures uMaterialTextures;

// Material texture flags uniform
uniform MaterialFlags uMaterialFlags;

uniform vec3 uLightDir;

void main() {
    // Sample base color from texture or use uniform
    vec4 baseColor = uMaterial.baseColor;
    if (uMaterialFlags.hasAlbedoTexture == 1) {
        baseColor = texture(uMaterialTextures.albedo, TexCoord) * uMaterial.baseColor;
    }

    // Alpha testing for MASK mode
    if (uMaterial.alphaMode == 1) { // MASK
        if (baseColor.a < uMaterial.alphaCutoff) {
            discard;
        }
    }

    // Sample normal from normal map if available
    vec3 norm = normalize(Normal);
    if (uMaterialFlags.hasNormalTexture == 1) {
        // Sample normal from texture and convert it from [0,1] -> [-1,1]
        vec3 tangentNormal = texture(uMaterialTextures.normal, TexCoord).xyz * 2.0 - 1.0;
        // Flip Y (DirectX → OpenGL)
        tangentNormal.y = -tangentNormal.y;
        norm = normalize(TBN * tangentNormal);
    }

    // Sample metallic-roughness from texture if available
    float metallic = uMaterial.metallicFactor;
    float roughness = uMaterial.roughnessFactor;
    if (uMaterialFlags.hasMetallicRoughnessTexture == 1) {
        vec4 mrSample = texture(uMaterialTextures.metallicRoughness, TexCoord);
        metallic = mrSample.r * uMaterial.metallicFactor;
        roughness = mrSample.g * uMaterial.roughnessFactor;
    }

    // Sample occlusion if available
    float occlusion = 1.0;
    if (uMaterialFlags.hasOcclusionTexture == 1) {
        occlusion = mix(1.0, texture(uMaterialTextures.occlusion, TexCoord).r, uMaterial.occlusionStrength);
    }

    // Sample emissive if available
    vec3 emissive = uMaterial.emissiveFactor;
    if (uMaterialFlags.hasEmissiveTexture == 1) {
        emissive = texture(uMaterialTextures.emissive, TexCoord).rgb * uMaterial.emissiveFactor;
    }

    // Simple lighting (can be replaced with PBR lighting later)
    vec3 lightDir = normalize(vec3(2.0, 0.0, 5.0));// Light direction
    lightDir = uLightDir;
    vec3 lightColor = vec3(1.0, 1.0, 1.0);// White light
    vec3 viewPos = vec3(0.0, 0.0, 5.0);// View position

    // Diffuse lighting
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // Ambient lighting
    vec3 ambient = vec3(0.35);

    // Specular lighting (roughness affects shininess)
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float shininess = (1.0 - roughness) * 128.0;// Convert roughness to shininess
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
    vec3 specular = spec * lightColor * (metallic * 0.5 + 0.3);

    // Combine lighting with base color
    vec3 litColor = (ambient * occlusion + diffuse + specular) * baseColor.rgb;

    // Add emissiveSD
    litColor += emissive;

    // Final color
    FragColor = vec4(litColor, baseColor.a);
}


//float DistributionGGX(vec3 N, vec3 H, float roughness) {
//    float a = roughness * roughness;
//    float a2 = a * a;
//    float NdotH = max(dot(N, H), 0.0);
//    float NdotH2 = NdotH * NdotH;
//
//    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
//    return a2 / (3.14159 * denom * denom);
//}
//
//float GeometrySchlickGGX(float NdotV, float roughness) {
//    float r = roughness + 1.0;
//    float k = (r * r) / 8.0;
//    return NdotV / (NdotV * (1.0 - k) + k);
//}
//
//float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
//    return GeometrySchlickGGX(max(dot(N, V), 0.0), roughness) *
//    GeometrySchlickGGX(max(dot(N, L), 0.0), roughness);
//}
//
//vec3 FresnelSchlick(float cosTheta, vec3 F0) {
//    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
//}


//    vec3 N = norm;
//    vec3 V = normalize(viewPos - FragPos);
//    vec3 L = normalize(lightDir);
//    vec3 H = normalize(V + L);
//
//    float NdotL = max(dot(N, L), 0.0);
//
//    // Fresnel reflectance at normal incidence
//    vec3 F0 = mix(vec3(0.04), baseColor.rgb, metallic);
//
//    // Cook-Torrance BRDF
//    float D = DistributionGGX(N, H, roughness);
//    float G = GeometrySmith(N, V, L, roughness);
//    vec3 F = FresnelSchlick(max(dot(H, V), 0.0), F0);
//
//    vec3 specular = (D * G * F) /
//    max(4.0 * max(dot(N, V), 0.0) * NdotL, 0.001);
//
//    // Energy conservation
//    vec3 kS = F;
//    vec3 kD = (1.0 - kS) * (1.0 - metallic);
//
//    vec3 diffuse = kD * baseColor.rgb / 3.14159;
//
//    vec3 color = (diffuse + specular) * lightColor * NdotL;
//    color += ambient * baseColor.rgb + emissive;
//
//    FragColor = vec4(color, baseColor.a);