#version 330 core

in vec2 TexCoords;
in vec3 Normal;
in vec3 FragPos;

out vec4 FragColor;

uniform sampler2D textureAtlas;
uniform vec3 lightPos;

void main() {
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(normalize(Normal), lightDir), 0.1);
    vec4 texColor = texture(textureAtlas, TexCoords);
    FragColor = texColor * diff;
}
