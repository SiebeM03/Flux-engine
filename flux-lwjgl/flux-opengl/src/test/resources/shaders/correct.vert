#version 330 core
layout (location = 0) in vec3 usedAttributeLoc0;
layout (location = 2) in float unusedAttribute;
layout (location = 8) in vec2 usedAttributeLoc8;

uniform mat4 usedUniformInVert;
uniform float unusedUniformInVert;

void main() {
    gl_Position = usedUniformInVert * vec4(usedAttributeLoc8.x, usedAttributeLoc0.y, usedAttributeLoc0.x, 0.0);
}