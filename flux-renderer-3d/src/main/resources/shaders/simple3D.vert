#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;

out vec4 fColor;

uniform mat4 uViewProj;

void main()
{
    fColor = aColor;
    gl_Position = uViewProj * vec4(aPos.x, aPos.y, aPos.z, 1.0);
}