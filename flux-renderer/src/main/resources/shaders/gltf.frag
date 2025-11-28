#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec3 Tangent;
in vec3 Bitangent;

out vec4 FragColor;

uniform vec4 uBaseColorFactor;
uniform vec3 uLightPosition;
uniform vec3 uLightColor;
uniform vec3 uViewPosition;

void main()
{
    // Simple Phong lighting
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(uLightPosition - FragPos);
    vec3 viewDir = normalize(uViewPosition - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    
    // Ambient
    float ambientStrength = 0.1;
    vec3 ambient = ambientStrength * uLightColor;
    
    // Diffuse
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * uLightColor;
    
    // Specular
    float specularStrength = 0.5;
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = specularStrength * spec * uLightColor;
    
    vec3 result = (ambient + diffuse + specular) * uBaseColorFactor.rgb;
    FragColor = vec4(result, uBaseColorFactor.a);
}


