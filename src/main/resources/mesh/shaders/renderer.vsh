#version 460 core

#define ONE_OVER_255 0.0039216
#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1

out float s_DRAW_BUFFER_INDEX;
out float s_TEXTURE_INDEX;
out vec2 s_INSTANCE_UV;
out vec2 s_SAMPLER_UV;
out vec4 s_VERTEX_COLOR;

struct RectInstance {
    vec2 pos1; // 8
    vec2 pos2; // 16

    int packedColorARGB; // 20
    int packedMatrices; // 24

    int textureIndex; // 28
    int _pad[1]; // 32
};

struct StringInstance {
    int packedMatrices;
    float height;
};

struct CharInstance {
    vec2 position; // 8
    int stringIndex; // 12
    int glyphIndex; // 16
};

struct Glyph {
    vec2 uv1; // 8
    vec2 uv2; // 16
};

layout (std430) readonly buffer ProjectionMatrixBuffer {
    mat4 projectionMatrices[];
} projectionMatrixAccess;

layout (std430) readonly buffer ViewMatrixBuffer {
    mat4 viewMatrices[];
} viewMatrixAccess;

layout (std430) readonly buffer ModelMatrixBuffer {
    mat4 modelMatrices[];
} modelMatrixAccess;

layout (std430) readonly buffer InstanceBuffer {
    int instances[];
} instanceAccess;

layout (std430) readonly buffer RectInstanceBuffer { // renderable #0
    RectInstance rectInstances[];
} rectAccess;

layout (std430) readonly buffer StringInstanceBuffer {
    StringInstance stringInstances[];
} stringAccess;

layout (std430) readonly buffer CharInstanceBuffer { // renderable #1
    CharInstance charInstances[];
} charAccess;

layout (std430) readonly buffer GlyphBuffer {
    Glyph glyphInstances[];
} glyphAccess;




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

void setVertexPosition(int packedMatrices, vec4 rawPosition) {
    int projectionMatrixIndex = (packedMatrices >> 28) & 0x0000000F;  //  4 bits
    int viewMatrixIndex       = (packedMatrices >> 8 ) & 0x000FFFFF;  // 20 bits
    int modelMatrixIndex      = (packedMatrices      ) & 0x000000FF;  //  8 bits

    mat4 projectionMatrix = projectionMatrixAccess.projectionMatrices[projectionMatrixIndex];
    mat4 viewMatrix = viewMatrixAccess.viewMatrices[viewMatrixIndex];
    mat4 modelMatrix = modelMatrixAccess.modelMatrices[modelMatrixIndex];

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * rawPosition;
}




void _RECT(RectInstance rectInstance, vec2 uv01) {
    vec4 rectPosition = vec4(mix(rectInstance.pos1, rectInstance.pos2, uv01), 0.0, 1.0);

    s_VERTEX_COLOR = unpackColorARGB(rectInstance.packedColorARGB);
    s_TEXTURE_INDEX = float(rectInstance.textureIndex);

    s_INSTANCE_UV = uv01;
    s_SAMPLER_UV = uv01;
    setVertexPosition(rectInstance.packedMatrices, rectPosition);
}

void _CHAR(CharInstance charInstance, vec2 uv01) {
    StringInstance stringInstance = stringAccess.stringInstances[charInstance.stringIndex];
    Glyph glyphInfo = glyphAccess.glyphInstances[charInstance.glyphIndex];

    vec2 uvSize = glyphInfo.uv2 - glyphInfo.uv1;
    float aspectRatio = uvSize.x / uvSize.y;
    float charWidth = stringInstance.height * aspectRatio;

    vec2 pos = charInstance.position;
    vec2 pos1 = pos - vec2(0.0, stringInstance.height);
    vec2 pos2 = pos + vec2(charWidth, 0.0);

    vec4 charPosition = vec4(mix(pos1, pos2, uv01), 0.0, 1.0);

    s_VERTEX_COLOR = vec4(1.0);
    s_TEXTURE_INDEX = 0.0;

    s_INSTANCE_UV = uv01;
    s_SAMPLER_UV = mix(glyphInfo.uv1, glyphInfo.uv2, uv01);
    setVertexPosition(stringInstance.packedMatrices, charPosition);
}




void main() {
    int packedInstance = instanceAccess.instances[gl_VertexID / 6];
    ivec2 instanceData = unpackInstanceData(packedInstance);
    int bufferIndex    = instanceData.x;
    int instanceIndex  = instanceData.y;

    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left

    s_DRAW_BUFFER_INDEX = float(bufferIndex);

    if (bufferIndex == RECT_BUFFER_INDEX) {
        RectInstance rect = rectAccess.rectInstances[instanceIndex];
        _RECT(rect, uv01);
        return;
    }

    if (bufferIndex == CHAR_BUFFER_INDEX) {
        CharInstance charInstance = charAccess.charInstances[instanceIndex];
        _CHAR(charInstance, uv01);
        return;
    }
}