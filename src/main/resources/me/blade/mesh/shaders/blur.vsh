#version 460 core

uniform mat4 u_PROJECTION_MATRIX;
uniform vec2 u_POS1;
uniform vec2 u_POS2;

out vec2 s_SAMPLER_UV;

void main() {
    int globalInstanceIndex = gl_VertexID / 6;
    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left

    vec2 pos = mix(u_POS1, u_POS2, uv01);
    gl_Position = u_PROJECTION_MATRIX * vec4(pos, 0.0, 1.0);
    s_SAMPLER_UV = gl_Position.xy * 0.5 + 0.5;;
}