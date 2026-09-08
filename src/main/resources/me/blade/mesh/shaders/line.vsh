#version 460 core

#define ONE_OVER_255 0.0039216
#define MAX_WIDTH 16
#define EXTEND_PIXELS (MAX_WIDTH + 4)

flat out vec2 s_RECT_POS1;
flat out vec2 s_RECT_POS2;
out vec2 s_FRAG_COORD;
out vec4 s_COLOR;
out float s_WIDTH;

layout (std430) readonly buffer MatrixBuffer {
    mat4 projection;
    mat4 view;
    mat4 model;
} matrixAccess;

struct LineInstance {
    vec2 pos1;
    vec2 pos2;
    int packedColor;
    float width;
    float _pad[2];
};

layout (std430) readonly buffer LineBuffer {
    LineInstance lines[];
} instanceAccess;

vec4 unpackColorARGB(int color) {
    float a = ((color >> 24) & 0xFF) * ONE_OVER_255;
    float r = ((color >> 16) & 0xFF) * ONE_OVER_255;
    float g = ((color >> 8)  & 0xFF) * ONE_OVER_255;
    float b = ((color >> 0)  & 0xFF) * ONE_OVER_255;
    return clamp(vec4(r, g, b, a), 0.0, 1.0);
}

void main() {
    int globalInstanceIndex = gl_VertexID / 6;
    LineInstance lineInstance = instanceAccess.lines[globalInstanceIndex];

    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left

    vec2 p1 = vec2(
        min(lineInstance.pos1.x, lineInstance.pos2.x),
        min(lineInstance.pos1.y, lineInstance.pos2.y)
    );

    vec2 p2 = vec2(
        max(lineInstance.pos1.x, lineInstance.pos2.x),
        max(lineInstance.pos1.y, lineInstance.pos2.y)
    );

    vec2 mappedPos = mix(p1 - EXTEND_PIXELS, p2 + EXTEND_PIXELS, uv01);

    s_RECT_POS1 = p1;
    s_RECT_POS2 = p2;
    s_FRAG_COORD = mappedPos;
    s_COLOR = unpackColorARGB(lineInstance.packedColor);
    s_WIDTH = lineInstance.width * 2;

    gl_Position = matrixAccess.projection * matrixAccess.view * matrixAccess.model * vec4(mappedPos, 0.0, 1.0);
}