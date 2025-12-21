#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;

out vec4 FragColor;

void main() {
    // Normalize the normal
    vec3 norm = normalize(Normal);

    // Simple hardcoded lighting (can be replaced with uniforms later)
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3)); // Light direction
    vec3 lightColor = vec3(1.0, 1.0, 1.0);          // White light
    vec3 viewPos = vec3(0.0, 0.0, 5.0);             // View position

    // Simple diffuse lighting
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // Simple ambient lighting
    vec3 ambient = vec3(0.2, 0.2, 0.2);

    // Simple specular lighting
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = spec * lightColor * 0.5;

    // Combine lighting with a neutral base color
    vec3 result = (ambient + diffuse + specular) * vec3(0.8, 0.8, 0.9);

    // Debug options (uncomment to visualize):
    // Visualize normals as colors:
    // FragColor = vec4(norm * 0.5 + 0.5, 1.0);

    // Visualize texture coordinates:
    // FragColor = vec4(TexCoord, 0.0, 1.0);

    // Final color with lighting
    FragColor = vec4(result, 1.0);
}