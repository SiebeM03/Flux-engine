#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
flat in float fTexId;

out vec4 color;
uniform sampler2D uTextures[8];

void main() {
    //    if (fTexId == 0) {
    //        color = vec4(1.0, 0.0, 1.0, 0.5);
    //    } else if (fTexId == 1) {
    //        color = vec4(1.0, 1.0, 0.0, 1.0);
    //    } else if (fTexId == 2) {
    //        color = vec4(1.0, 1.0, 1.0, 1.0);
    //    } else {
    //        color = vec4(0, 1, 0, 1);
    //    }
    if (fTexId < 0.5) {
        color = fColor;
    } else {
        color = fColor * texture(uTextures[int(fTexId) + 2], fTexCoords);
    }
    //    color = texture(uTextures[5], fTexCoords);


    //    color = vec4(float(fTexId) / 4, 0, 0, 1);

    //    } else if (fTexId == 1) {
    //        color = vec4(0.0, 1.0, 1.0, 1.0);
    //    } else if (fTexId == 2) {
    //        color = vec4(1.0, 0.0, 1.0, 1.0);
    //    } else if (fTexId == 3) {
    //        color = vec4(1.0, 1.0, 0.0, 1.0);
    //    }
    //        color = vec4(int(fTexId) / 2, 0, 0, 1.0);
}