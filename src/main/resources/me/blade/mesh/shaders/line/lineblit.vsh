#version 460 core

out vec2 s_UV01;

void main() {
    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left
    s_UV01 = uv01;

    // initially uv01 is 0.0..1.0
    uv01 *= 2; // map to 0.0..2.0
    uv01 -= 1; // map to -1.0..1.0
    gl_Position = vec4(uv01, 0.0, 1.0);
}