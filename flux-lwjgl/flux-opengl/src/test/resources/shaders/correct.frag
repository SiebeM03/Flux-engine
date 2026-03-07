#version 330 core

uniform vec4 usedUniformInFrag;
uniform mat4 unusedUniformInFrag;
uniform float[3] arrayUniform;

out vec4 color;

void main() {
    color = vec4(arrayUniform[2], usedUniformInFrag);
}