#version 330 core

in vec2 vUV;
out vec4 FragColor;

uniform sampler2D uWoodAlbedo;
uniform sampler2D uSkin;

void main()
{
    vec4 wood = texture(uWoodAlbedo, vUV);
    vec4 skin = texture(uSkin, vUV);

    // Alpha = mask
    vec3 finalColor = mix(wood.rgb, skin.rgb, wood.a < 1.0 ? 1.0 : 0.0);

    if (wood.a < 1.0) {
        FragColor = vec4(skin.rgb, 1.0);
    } else {
        FragColor = vec4(wood.rgb, 1.0);
    }
}
