#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec4 aTangent;

out vec3 FragPos;
out vec3 Normal;
out vec2 TexCoord;

uniform mat4 uViewProj;
uniform mat4 uModelMatrix;

void main() {
    // Transform position to world space
    vec4 worldPos = uModelMatrix * vec4(aPos, 1.0);
    FragPos = worldPos.xyz;

    // Transform normal to world space (using normal matrix)
    // For simplicity, we'll use the model matrix's inverse transpose
    // In a production shader, you'd want to pass a precomputed normal matrix
    mat3 normalMatrix = mat3(transpose(inverse(uModelMatrix)));
    Normal = normalize(normalMatrix * aNormal);

    // Pass through texture coordinates
    TexCoord = aTexCoord;

    // Transform to clip space
    gl_Position = uViewProj * worldPos;
}
