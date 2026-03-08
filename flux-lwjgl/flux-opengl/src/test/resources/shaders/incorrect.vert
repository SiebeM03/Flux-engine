#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in float unusedAttribute;

uniform mat4 usedUniform;
uniform float unusedUniform

void main() {
    gl_Position = usedUniform * vec4(aPos, 0.0);
}