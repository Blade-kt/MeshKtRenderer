#version 460 core

#define ONE_OVER_255 0.0039216
#define RECT_BUFFER_INDEX 0
#define CHAR_BUFFER_INDEX 1
#define CHAR_EXTEND 32

struct ScissorData {
    vec2 pos1;
    vec2 pos2;
};

uniform float u_FONT_DIM_SIZE;

flat out int s_DRAW_BUFFER_INDEX;
flat out int s_TEXTURE_INDEX;
out vec2 s_INSTANCE_UV;
out vec2 s_SAMPLER_UV;
out vec4 s_VERTEX_COLOR;
out vec4 s_ROUND_RADIUS;
out vec2 s_RECT_SIZE;
out ScissorData s_SCISSOR_DATA;
out vec2 s_RAW_POSITION;

struct ColorData {
    int packedColorLeftTopARGB;
    int packedColorRightTopARGB;
    int packedColorRightBottomARGB;
    int packedColorLeftBottomARGB;
};

struct RectInstance {
    vec2 pos1; // 8
    vec2 pos2; // 16

    int colorDataIndex; // 20
    int packedRoundRadius; // 24

    int packedMatrices; // 28
    int textureIndex; // 32
};

struct StringInstance {
    int packedColorARGB;
    int packedMatrices;
    int textureIndex;
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

layout (std430) readonly buffer ScissorDataBuffer {
    ScissorData scissorData[];
} scissorStackAccess;

layout (std430) readonly buffer ScissorIndexBuffer {
    int scissorIndices[];
} scissorIndexAccess;

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

layout (std430) readonly buffer ColorBuffer {
    ColorData colorInstances[];
} colorAccess;

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

vec4 unpackRoundRadius(int radiuses) {
    float a = ((radiuses >> 24) & 0xFF) * 0.5;
    float b = ((radiuses >> 16) & 0xFF) * 0.5;
    float c = ((radiuses >> 8)  & 0xFF) * 0.5;
    float d = ((radiuses >> 0)  & 0xFF) * 0.5;
    return vec4(a, b, c, d);
}

ivec2 unpackInstanceData(int packedData) {
    int first   = (packedData >> 28) & 0x0000000F; //  4 bits
    int second  = (packedData      ) & 0x0FFFFFFF; // 28 bits
    return ivec2(first, second);
}

void setVertexPosition(int packedMatrices, vec2 rawPosition) {
    int projectionMatrixIndex = (packedMatrices >> 28) & 0x0000000F;  //  4 bits
    int viewMatrixIndex       = (packedMatrices >> 8 ) & 0x000FFFFF;  // 20 bits
    int modelMatrixIndex      = (packedMatrices      ) & 0x000000FF;  //  8 bits

    mat4 projectionMatrix = projectionMatrixAccess.projectionMatrices[projectionMatrixIndex];
    mat4 viewMatrix = viewMatrixAccess.viewMatrices[viewMatrixIndex];
    mat4 modelMatrix = modelMatrixAccess.modelMatrices[modelMatrixIndex];

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(rawPosition, 0.0, 1.0);
    s_RAW_POSITION = rawPosition;
}




void _RECT(RectInstance rectInstance, vec2 uv01) {
    ColorData colorData = colorAccess.colorInstances[rectInstance.colorDataIndex];
    vec4 colorLeftTop = unpackColorARGB(colorData.packedColorLeftTopARGB);
    vec4 colorRightTop = unpackColorARGB(colorData.packedColorRightTopARGB);
    vec4 colorRightBottom = unpackColorARGB(colorData.packedColorRightBottomARGB);
    vec4 colorLeftBottom = unpackColorARGB(colorData.packedColorLeftBottomARGB);

    s_VERTEX_COLOR = mix(
        mix(colorLeftTop, colorRightTop, uv01.x),
        mix(colorLeftBottom, colorRightBottom, uv01.x),
        uv01.y
    );
    s_ROUND_RADIUS = unpackRoundRadius(rectInstance.packedRoundRadius);

    s_TEXTURE_INDEX = rectInstance.textureIndex;

    s_INSTANCE_UV = uv01;
    s_SAMPLER_UV = uv01;
    s_RECT_SIZE = rectInstance.pos2 - rectInstance.pos1;

    vec2 rectPosition = mix(rectInstance.pos1, rectInstance.pos2, uv01);
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
    vec2 size = pos2 - pos1;

    s_VERTEX_COLOR = unpackColorARGB(stringInstance.packedColorARGB);
    s_TEXTURE_INDEX = stringInstance.textureIndex;
    s_RECT_SIZE = size;

    float extend = CHAR_EXTEND * (stringInstance.height / u_FONT_DIM_SIZE);
    vec2 texelSize = vec2(1.0) / size;
    vec2 uvExtend = extend * texelSize;
    s_INSTANCE_UV     = mix(-uvExtend, vec2(1.0) + uvExtend, uv01);
    s_SAMPLER_UV      = mix(glyphInfo.uv1, glyphInfo.uv2, s_INSTANCE_UV);
    vec2 charPosition = mix(pos1 - extend, pos2 - extend, uv01);
    setVertexPosition(stringInstance.packedMatrices, charPosition);
}




void main() {
    int globalInstanceIndex = gl_VertexID / 6;
    int packedInstance = instanceAccess.instances[globalInstanceIndex];
    ivec2 instanceData = unpackInstanceData(packedInstance);
    int bufferIndex    = instanceData.x;
    int instanceIndex  = instanceData.y;

    int vertexIndex = gl_VertexID % 6;   vec2 uv01 = vec2(1.0, 1.0); // top-right (i == 2 or i == 4)
    if (vertexIndex == 5)                     uv01 = vec2(1.0, 0.0); // bottom-right
    if (vertexIndex == 1)                     uv01 = vec2(0.0, 1.0); // top-left
    if (vertexIndex == 0 || vertexIndex == 3) uv01 = vec2(0.0, 0.0); // bottom-left

    s_DRAW_BUFFER_INDEX = bufferIndex;

    int scissorIndex = scissorIndexAccess.scissorIndices[globalInstanceIndex];
    if (scissorIndex >= 0) {
        s_SCISSOR_DATA = scissorStackAccess.scissorData[scissorIndex];
    } else {
        s_SCISSOR_DATA = ScissorData(vec2(-100000.0), vec2(100000.0));
    }

    if (bufferIndex == RECT_BUFFER_INDEX) {
        RectInstance rectInstance = rectAccess.rectInstances[instanceIndex];
        _RECT(rectInstance, uv01);
        return;
    }

    if (bufferIndex == CHAR_BUFFER_INDEX) {
        CharInstance charInstance = charAccess.charInstances[instanceIndex];
        _CHAR(charInstance, uv01);
        return;
    }
}