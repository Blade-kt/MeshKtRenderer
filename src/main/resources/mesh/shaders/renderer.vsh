#version 460 core

#define ONE_OVER_255 0.0039216

out float s_DRAW_BUFFER_INDEX;
out vec4 s_VERTEX_COLOR;

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

layout (std430) buffer InstanceBuffer {
    int instances[];
} instanceAccess;

layout (std430) buffer RectBuffer {
    RectInstance rectInstances[];
} rectAccess;




vec4 unpackColorARGB(int color) {
    float a = ((color >> 24) & 0xFF) * ONE_OVER_255;
    float r = ((color >> 16) & 0xFF) * ONE_OVER_255;
    float g = ((color >> 8)  & 0xFF) * ONE_OVER_255;
    float b = ((color >> 0)  & 0xFF) * ONE_OVER_255;
    return clamp(vec4(r, g, b, a), 0.0, 1.0);
}

ivec2 unpackInstanceData(int packedData) {
    int first   = (packedData >> 28) & 0x0000000F; //  4 bits
    int second  = (packedData      ) & 0x0FFFFFFF; // 28 bits
    return ivec2(first, second);
}

ivec3 unpackMatrices(int packedMatrices) {
    int projectionMatrix = (packedMatrices >> 28) & 0x0000000F;  //  4 bits
    int viewMatrix       = (packedMatrices >> 8 ) & 0x00FFFFFF;  // 20 bits
    int modelMatrix      = (packedMatrices      ) & 0x000000FF;  //  8 bits
    return ivec3(projectionMatrix, viewMatrix, modelMatrix);
}




void RECT(RectInstance rectInstance, vec2 uv01) {
    ivec3 matricesUnpacked = unpackMatrices(rectInstance.packedMatrices);
    mat4 projectionMatrix = matrixAccess.matrices[matricesUnpacked.x];
    mat4 viewMatrix       = matrixAccess.matrices[matricesUnpacked.y];
    mat4 modelMatrix      = matrixAccess.matrices[matricesUnpacked.z];
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(mix(rectInstance.pos1, rectInstance.pos2, uv01), 0.0, 1.0);

    s_VERTEX_COLOR = unpackColorARGB(rectInstance.packedColorARGB);
}




void main() {
    int packedInstance = instanceAccess.instances[gl_VertexID / 6];
    ivec2 instanceData = unpackInstanceData(packedInstance);
    int bufferIndex    = instanceData.x;
    int instanceIndex  = instanceData.y;

    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0);      // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0);      // bottom-left

    s_DRAW_BUFFER_INDEX = float(bufferIndex);

    if (bufferIndex == 0) {
        RectInstance rect = rectAccess.rectInstances[instanceIndex];
        RECT(rect, uv01);
        return;
    }
}