#version 460 core

uniform mat4 u_MATRIX;
uniform vec2 u_SRC_SIZE;
uniform vec2 u_DST_SIZE;

out vec2 s_UV;

void main() {
    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left

    s_UV = uv01;
    gl_Position = u_MATRIX * vec4(mix(vec2(0.0), u_DST_SIZE, uv01), 0.0, 1.0);
}