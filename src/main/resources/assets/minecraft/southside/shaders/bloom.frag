#version 120

uniform sampler2D inTexture;
uniform vec2 texelSize, direction;
uniform float radius;
uniform float weights[256];

#define offset texelSize * direction

void main() {
    vec4 color = texture2D(inTexture, gl_TexCoord[0].st) * weights[0];
    float totalWeight = weights[0];

    for (float f = 1.0; f <= radius; f++) {
        color += texture2D(inTexture, gl_TexCoord[0].st + f * offset) * weights[int(abs(f))];
        color += texture2D(inTexture, gl_TexCoord[0].st - f * offset) * weights[int(abs(f))];
        totalWeight += weights[int(abs(f))] * 2.0;
    }

    gl_FragColor = color / totalWeight;
}
