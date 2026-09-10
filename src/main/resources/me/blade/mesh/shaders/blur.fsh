#version 460 core
#extension GL_ARB_bindless_texture : require

uniform vec2 u_TEXEL_SIZE;
uniform vec2 u_DIRECTION;
layout(bindless_sampler) uniform sampler2D u_INPUT_TEXTURE;

in vec2 s_SAMPLER_UV;
out layout(location = 0) vec4 COLOR_ATTACHMENT0;

const float WEIGHTS[5] = float[](
    0.2270270270,
    0.1945945946,
    0.1216216216,
    0.0540540541,
    0.0162162162
);

void main() {
    vec2 offset = u_DIRECTION * u_TEXEL_SIZE;

    vec4 original = texture(u_INPUT_TEXTURE, s_SAMPLER_UV);
    vec4 result = original * WEIGHTS[0];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset) * WEIGHTS[1];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset) * WEIGHTS[1];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 2) * WEIGHTS[2];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 2) * WEIGHTS[2];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 3) * WEIGHTS[3];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 3) * WEIGHTS[3];

    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV + offset * 4) * WEIGHTS[4];
    result += texture(u_INPUT_TEXTURE, s_SAMPLER_UV - offset * 4) * WEIGHTS[4];

    COLOR_ATTACHMENT0 = result;
}