#version 460 core
#extension GL_ARB_bindless_texture : require

#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1
#define SMOOTHING_BIAS 20

struct ScissorData {
    vec2 pos1;
    vec2 pos2;
};

flat in int s_DRAW_BUFFER_INDEX;
flat in int s_TEXTURE_INDEX;
flat in vec4 s_ROUND_RADIUS;
flat in vec2 s_RECT_SIZE;
flat in ScissorData s_SCISSOR_DATA;
flat in float s_FONT_HEIGHT;

in vec2 s_INSTANCE_UV;
in vec2 s_SAMPLER_UV;
in vec4 s_VERTEX_COLOR;
in vec2 s_RAW_POSITION;

out layout(location = 0) vec4 COLOR_ATTACHMENT0;
//out layout(location = 1) float SDF_ATTACHMENT;

layout (std430) readonly buffer TextureHandleBuffer {
    sampler2D handleArray[];
} textureAccess;

vec4 scissorCut() {
    vec2 center = (s_SCISSOR_DATA.pos1 + s_SCISSOR_DATA.pos2) * 0.5;
    vec2 half_size = (s_SCISSOR_DATA.pos2 - s_SCISSOR_DATA.pos1) * 0.5;
    vec2 d = abs(s_RAW_POSITION.xy - center) - half_size;
    float sdf = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
    return vec4(1.0, 1.0, 1.0, 1.0 - smoothstep(0.0, 1.0, sdf));
}

float linearstep(float edge0, float edge1, float x) {
    return clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
}

void main() {
    vec4 textureColor = vec4(1.0, 1.0, 1.0, 1.0);

    if (s_TEXTURE_INDEX != -1) {
        textureColor = texture(sampler2D(textureAccess.handleArray[s_TEXTURE_INDEX]), s_SAMPLER_UV);
    }

    vec4 scissor = scissorCut();

    if (s_DRAW_BUFFER_INDEX == RECT_BUFFER_INDEX) {
        vec4 r = s_ROUND_RADIUS * 0.5;
        r.xy = (s_INSTANCE_UV.x > 0.5) ? r.xy : r.zw;
        r.x  = (s_INSTANCE_UV.y > 0.5) ? r.x  : r.y;

        vec2 q = s_RECT_SIZE * 0.5 * (abs(s_INSTANCE_UV - 0.5) - 0.5) + r.x;
        float sdf = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;

        float smoothness = fwidth(sdf * 0.5 + 0.5);
        float alpha = 1.0 - linearstep(-smoothness, smoothness, sdf);
        vec4 rectColor = vec4(1.0, 1.0, 1.0, alpha);

        COLOR_ATTACHMENT0 = s_VERTEX_COLOR * textureColor * rectColor * scissor;
    }

    if (s_DRAW_BUFFER_INDEX == CHAR_BUFFER_INDEX) {
        float sdf = textureColor.r * 2 - 1;
        float smoothness = 1.0 - linearstep(0.0, s_FONT_HEIGHT + SMOOTHING_BIAS, s_FONT_HEIGHT);
        float alpha = linearstep(-smoothness, smoothness, sdf);
        vec4 fontColor = vec4(1.0, 1.0, 1.0, alpha);
        COLOR_ATTACHMENT0 = s_VERTEX_COLOR * fontColor * scissor;
    }
}