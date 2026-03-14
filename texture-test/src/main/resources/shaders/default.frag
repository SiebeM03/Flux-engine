#version 330 core

in vec2 fTexCoords;
in float fTexId;

uniform sampler2D uTextures[8];

out vec4 color;

void main() {
    vec4 texColor;
    if (max(0, min(fTexId - 1, floor(fTexId + 0.5))) == 0) {
        texColor = vec4(0, 1, 1, 1);    // Cyan
    } else if (max(0, min(fTexId - 1, floor(fTexId + 0.5))) == 1) {
        texColor = texture(uTextures[1], fTexCoords) * vec4(1, 1, 1, 1);
        //        texColor = vec4(1, 0, 1, 1);    // Magenta
    } else if (max(0, min(fTexId - 1, floor(fTexId + 0.5))) == 2) {
        texColor = texture(uTextures[1], fTexCoords) * vec4(1, 1, 1, 1);
        //        texColor = vec4(1, 1, 1, 1);    // White
    } else if (max(0, min(fTexId - 1, floor(fTexId + 0.5))) == 3) {
        texColor = texture(uTextures[2], fTexCoords) * vec4(1, 1, 1, 1);
        texColor = vec4(0, 0, 1, 1);    // Blue
    } else {
        texColor = vec4(1, 0, 0, 1);    // Red
    }

    color = texColor;
}