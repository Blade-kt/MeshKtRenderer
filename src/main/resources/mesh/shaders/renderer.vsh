#version 460 core

out float S_BUFFER_INDEX;
out vec4 S_VERTEX_COLOR;

struct RectInstance {
    vec2 pos1; // 8
    vec2 pos2; // 16

    int packedColorARGB; // 24
    int packedMatrices; // 28

    float _pad[2]; // 32
};

layout (std430) buffer MatrixBuffer {
    mat4 matrices[];
} matrixAccess;

layout (std430) buffer InstancePointerBuffer {
    int pointers[];
} pointerAccess;

layout (std430) buffer RectBuffer {
    RectInstance rectInstances[];
} rectAccess;

vec4 unpackColor(int color) {
    float r = ((color >> 16) & 0xFF) * 0.0039216;
    float g = ((color >> 8)  & 0xFF) * 0.0039216;
    float b = ((color >> 0)  & 0xFF) * 0.0039216;
    float a = ((color >> 24) & 0xFF) * 0.0039216;
    return clamp(vec4(r, g, b, a), 0.0, 1.0);
}

ivec3 unpackMatrices(int packedMatrices) {
    int projectionMatrix = (packedMatrices >> 28) & 0x0000000F;  // first 4 bits
    int viewMatrix       = (packedMatrices >> 8)  & 0x00FFFFFF;  // other 20 bits
    int modelMatrix      =  packedMatrices        & 0x000000FF;  // other 16 bits
    return ivec3(projectionMatrix, viewMatrix, modelMatrix);
}

void _RECT(RectInstance rectInstance, vec2 uv01) {
    ivec3 matricesUnpacked = unpackMatrices(rectInstance.packedMatrices);
    mat4 projectionMatrix = matrixAccess.matrices[matricesUnpacked.x];
    mat4 viewMatrix = matrixAccess.matrices[matricesUnpacked.y];
    mat4 modelMatrix = matrixAccess.matrices[matricesUnpacked.z];
    gl_Position = projectionMatrix * viewMatrix * vec4(mix(rectInstance.pos1, rectInstance.pos2, uv01), 0.0, 1.0);

    S_VERTEX_COLOR = unpackColor(rectInstance.packedColorARGB);
}

void main() {
    int instancePointerIndex = gl_VertexID / 6;
    int vertexIndex = gl_VertexID % 6;

    int packedInteger = pointerAccess.pointers[instancePointerIndex];

    int bufferIndex   = (packedInteger >> 28) & 0x0000000F; // first 4 bits
    int instanceIndex =  packedInteger        & 0x0FFFFFFF; // other 28 bits

    vec2 uv01 = vec2(1.0);                                      // top-right (i == 2 or i == 4)
    if (vertexIndex == 5) uv01 = vec2(1.0, 0.0);                // bottom-right
    if (vertexIndex == 1) uv01 = vec2(0.0, 1.0);                // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0); // bottom-left

    S_BUFFER_INDEX = float(bufferIndex);

    if (bufferIndex == 0) {
        RectInstance rect = rectAccess.rectInstances[instanceIndex];
        _RECT(rect, uv01);
        return;
    }
}