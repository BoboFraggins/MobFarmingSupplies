#version 330

// Writes only depth, wherever a glow is at least DEPTH_CUTOUT opaque, so that clouds and weather,
// drawn after it, are hidden behind its brighter parts but still show through its faint edges.

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    if (texture(Sampler0, texCoord0).a * vertexColor.a < DEPTH_CUTOUT) {
        discard;
    }
    fragColor = vec4(0.0);
}
