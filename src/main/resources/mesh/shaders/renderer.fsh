#version 460 core

#extension GL_ARB_bindless_texture : require

#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1

uniform float u_NORMALIZED_FONT_BASELINE;

in float s_DRAW_BUFFER_INDEX;
in float s_TEXTURE_INDEX;

in vec2 s_INSTANCE_UV;
in vec2 s_SAMPLER_UV;

in vec4 s_VERTEX_COLOR;

out vec4 COLOR_ATTACHMENT0;

layout(std430) readonly buffer TextureBuffer {
    sampler2D handleArray[];
} textureAccess;

vec4 sampleTexture(vec2 uv) {
    int textureIndex = int(s_TEXTURE_INDEX);
    sampler2D bindlessSampler = sampler2D(textureAccess.handleArray[textureIndex]);
    return texture(bindlessSampler, uv);
}

void main() {
    int BUFFER_INDEX = int(s_DRAW_BUFFER_INDEX);
    vec4 textureColor = sampleTexture(s_SAMPLER_UV);

    if (BUFFER_INDEX == RECT_BUFFER_INDEX) {
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR;
        return;
    }

    /**if (s_INSTANCE_UV.y > u_NORMALIZED_FONT_BASELINE) {
        fontColor *= vec4(0.5, 0.5, 0.5, 1.0);
    }*/
    if (BUFFER_INDEX == CHAR_BUFFER_INDEX) {
        float rawSDF = textureColor.r;
        float smoothness = fwidth(rawSDF) * 0.5;
        float sdf = smoothstep(0.5 - smoothness, 0.5 + smoothness, textureColor.r);
        vec4 fontColor = vec4(1.0, 1.0, 1.0, sdf);
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR * fontColor;
        return;
    }
}