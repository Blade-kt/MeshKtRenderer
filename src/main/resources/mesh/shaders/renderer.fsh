#version 460 core

#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1

uniform sampler2D u_TEXTURE0;

in float s_DRAW_BUFFER_INDEX;
in float s_TEXTURE_INDEX;
in vec2 s_INSTANCE_UV;
in vec2 s_SAMPLER_UV;
in vec4 s_VERTEX_COLOR;

out vec4 COLOR_ATTACHMENT0;

vec4 sampleTexture(vec2 uv) {
    int textureIndex = int(s_TEXTURE_INDEX);
    return texture(u_TEXTURE0, uv);
}

void main() {
    int BUFFER_INDEX = int(s_DRAW_BUFFER_INDEX);
    vec4 textureColor = sampleTexture(s_SAMPLER_UV);

    if (BUFFER_INDEX == RECT_BUFFER_INDEX) {
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR;
    }

    if (BUFFER_INDEX == CHAR_BUFFER_INDEX) {
        float sdf = textureColor.r;
        float smoothness = fwidth(sdf) * 0.75;
        float alpha = smoothstep(0.5 - smoothness, 0.5 + smoothness, sdf);
        vec4 fontColor = vec4(1.0, 1.0, 1.0, alpha);
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR * fontColor;
    }

    if ((s_INSTANCE_UV.x > 1.0) || (s_INSTANCE_UV.x < 0.0) || (s_INSTANCE_UV.y > 1.0) || (s_INSTANCE_UV.y < 0.0)) {
        COLOR_ATTACHMENT0 = vec4(1, 0, 0, 1);
    }
}