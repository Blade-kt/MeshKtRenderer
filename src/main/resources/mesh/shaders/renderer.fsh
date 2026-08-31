#version 460 core

#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1

uniform float u_NORMALIZED_FONT_TOPLINE;
uniform float u_NORMALIZED_FONT_BASELINE;

uniform sampler2D TEXTURE_SAMPLER0;
uniform sampler2D TEXTURE_SAMPLER1;
uniform sampler2D TEXTURE_SAMPLER2;
uniform sampler2D TEXTURE_SAMPLER3;
uniform sampler2D TEXTURE_SAMPLER4;
uniform sampler2D TEXTURE_SAMPLER5;
uniform sampler2D TEXTURE_SAMPLER6;
uniform sampler2D TEXTURE_SAMPLER7;
uniform sampler2D TEXTURE_SAMPLER8;
uniform sampler2D TEXTURE_SAMPLER9;
uniform sampler2D TEXTURE_SAMPLER10;
uniform sampler2D TEXTURE_SAMPLER11;
uniform sampler2D TEXTURE_SAMPLER12;
uniform sampler2D TEXTURE_SAMPLER13;
uniform sampler2D TEXTURE_SAMPLER14;
uniform sampler2D TEXTURE_SAMPLER15;

in float s_DRAW_BUFFER_INDEX;
in float s_TEXTURE_INDEX;

in vec2 s_INSTANCE_UV;
in vec2 s_SAMPLER_UV;

in vec4 s_VERTEX_COLOR;

out vec4 COLOR_ATTACHMENT0;

vec4 sampleTexture(vec2 uv) {
    int textureIndex = int(s_TEXTURE_INDEX);
    if (textureIndex ==  0) return texture(TEXTURE_SAMPLER0,  uv);
    if (textureIndex ==  1) return texture(TEXTURE_SAMPLER1,  uv);
    if (textureIndex ==  2) return texture(TEXTURE_SAMPLER2,  uv);
    if (textureIndex ==  3) return texture(TEXTURE_SAMPLER3,  uv);
    if (textureIndex ==  4) return texture(TEXTURE_SAMPLER4,  uv);
    if (textureIndex ==  5) return texture(TEXTURE_SAMPLER5,  uv);
    if (textureIndex ==  6) return texture(TEXTURE_SAMPLER6,  uv);
    if (textureIndex ==  7) return texture(TEXTURE_SAMPLER7,  uv);
    if (textureIndex ==  8) return texture(TEXTURE_SAMPLER8,  uv);
    if (textureIndex ==  9) return texture(TEXTURE_SAMPLER9,  uv);
    if (textureIndex == 10) return texture(TEXTURE_SAMPLER10, uv);
    if (textureIndex == 11) return texture(TEXTURE_SAMPLER11, uv);
    if (textureIndex == 12) return texture(TEXTURE_SAMPLER12, uv);
    if (textureIndex == 13) return texture(TEXTURE_SAMPLER13, uv);
    if (textureIndex == 14) return texture(TEXTURE_SAMPLER14, uv);
    if (textureIndex == 15) return texture(TEXTURE_SAMPLER15, uv);
    return vec4(1, 1, 1, 1);
}

void main() {
    int BUFFER_INDEX = int(s_DRAW_BUFFER_INDEX);
    vec4 textureColor = sampleTexture(s_SAMPLER_UV);

    if (BUFFER_INDEX == RECT_BUFFER_INDEX) {
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR;
        return;
    }

    if (BUFFER_INDEX == CHAR_BUFFER_INDEX) {
        float smoothness = fwidth(textureColor.r) * 0.5;
        float sdf = smoothstep(0.5 - smoothness, 0.5 + smoothness, textureColor.r);
        vec4 fontColor = mix(vec4(1.0, 0.3, 0.3, 1.0), vec4(1.0), sdf);
        if (s_INSTANCE_UV.y < u_NORMALIZED_FONT_TOPLINE) {
            fontColor *= vec4(0.5, 0.5, 0.5, 1.0);
        }

        if (s_INSTANCE_UV.y > u_NORMALIZED_FONT_BASELINE) {
            fontColor *= vec4(0.5, 0.5, 0.5, 1.0);
        }
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR * fontColor;
        return;
    }
}