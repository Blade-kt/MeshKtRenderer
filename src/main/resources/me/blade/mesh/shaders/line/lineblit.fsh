#version 460 core
#extension GL_ARB_bindless_texture : require

layout(bindless_sampler) uniform sampler2D COLOR_INPUT_TEXTURE;
layout(bindless_sampler) uniform sampler2D SDF_INPUT_TEXTURE;

in vec2 s_UV01;

out layout(location = 0) vec4 COLOR_ATTACHMENT0;

void main() {
    vec4 COLOR_INPUT = texture(COLOR_INPUT_TEXTURE, s_UV01);
    float SDF_INPUT = texture(SDF_INPUT_TEXTURE, s_UV01).r;
    float alpha = smoothstep(0.3, 0.7, SDF_INPUT);

    COLOR_ATTACHMENT0 = COLOR_INPUT * vec4(1.0, 1.0, 1.0, alpha);
}