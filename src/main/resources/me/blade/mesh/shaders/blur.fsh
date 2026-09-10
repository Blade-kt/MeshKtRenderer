#version 460 core
#extension GL_ARB_bindless_texture : require

uniform vec2 u_TEXEL_SIZE;
uniform vec2 u_DIRECTION;
uniform vec4 u_ROUND_RADIUS;
uniform int u_IS_FLUSH_DRAW;
layout(bindless_sampler) uniform sampler2D u_INPUT_TEXTURE;

flat in vec2 s_MIN_UV;
flat in vec2 s_MAX_UV;
flat in vec2 s_RECT_SIZE;
in vec2 s_SAMPLER_UV;
in vec2 s_INSTANCE_UV;

out layout(location = 0) vec4 COLOR_ATTACHMENT0;

const float WEIGHTS[5] = float[](
    0.2270270270,
    0.1945945946,
    0.1216216216,
    0.0540540541,
    0.0162162162
);

float linearstep(float edge0, float edge1, float x) {
    return clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
}

void main() {
    vec2 offset = u_DIRECTION * u_TEXEL_SIZE;

    vec4 result = texture(u_INPUT_TEXTURE, s_SAMPLER_UV) * WEIGHTS[0];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset) * WEIGHTS[1];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset) * WEIGHTS[1];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 2) * WEIGHTS[2];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 2) * WEIGHTS[2];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 3) * WEIGHTS[3];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 3) * WEIGHTS[3];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 4) * WEIGHTS[4];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 4) * WEIGHTS[4];

    vec4 r = u_ROUND_RADIUS * 0.5;
    r.xy = (s_INSTANCE_UV.x > 0.5) ? r.xy : r.zw;
    r.x  = (s_INSTANCE_UV.y > 0.5) ? r.x  : r.y;

    vec2 q = s_RECT_SIZE * 0.5 * (abs(s_INSTANCE_UV - 0.5) - 0.5) + r.x;
    float sdf = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;

    float smoothness = fwidth(sdf * 0.5 + 0.5);
    float alpha = 1.0 - linearstep(-smoothness, smoothness, sdf) * u_IS_FLUSH_DRAW;

    COLOR_ATTACHMENT0 = result * vec4(1.0, 1.0, 1.0, 1.0);
}