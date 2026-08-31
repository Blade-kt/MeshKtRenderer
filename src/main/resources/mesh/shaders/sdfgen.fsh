#version 460 core

#extension GL_ARB_bindless_texture : require

uniform vec2 u_SRC_SIZE;
uniform vec2 u_DST_SIZE;

uniform int u_SDF_DOWNSCALE;
uniform int u_SDF_SCAN;

in vec2 s_UV;

out vec4 COLOR_ATTACHMENT0;

layout(std430) readonly buffer TextureBuffer {
    sampler2D inputTexture;
} textureAccess;

float distanceSquared(vec2 a, vec2 b) {
    vec2 diff = a - b;
    return dot(diff, diff);
}

bool isWhite(sampler2D inputSampler, vec2 pixel) {
    return texture(inputSampler, pixel / u_SRC_SIZE).r > 0.5;
}

void main() {
    sampler2D s = sampler2D(textureAccess.inputTexture);
    int srcX = int(s_UV.x * u_SRC_SIZE.x);
    int srcY = int(s_UV.y * u_SRC_SIZE.y);

    float distanceSq = u_SDF_SCAN * u_SDF_SCAN;

    for (int offsetX = -u_SDF_SCAN; offsetX < u_SDF_SCAN; offsetX++) {
        for (int offsetY = -u_SDF_SCAN; offsetY < u_SDF_SCAN; offsetY++) {
            float readX = srcX + offsetX;
            float readY = srcY + offsetY;

            bool centerTopWhite = isWhite(s, vec2(readX + 0, readY - 1));
            bool centerLeftWhite = isWhite(s, vec2(readX - 1, readY + 0));
            bool centerRightWhite = isWhite(s, vec2(readX + 1, readY + 0));
            bool centerBottomWhite = isWhite(s, vec2(readX + 0, readY + 1));
            bool hasBlackNeighbor = !centerTopWhite || !centerLeftWhite || !centerRightWhite || !centerBottomWhite;
            bool isCorner = isWhite(s, vec2(readX, readY)) && hasBlackNeighbor;

            if (isCorner) {
                distanceSq = min(distanceSq, distanceSquared(vec2(srcX, srcY), vec2(readX, readY)));
            }
        }
    }

    float insideSign = isWhite(s, vec2(srcX, srcY)) ? 1.0 : -1.0;
    float distance = sqrt(distanceSq);
    float normalizedDistance = distance / (u_SDF_SCAN * sqrt(2.0));
    float sdf = 0.5 + normalizedDistance * insideSign;

    COLOR_ATTACHMENT0 = vec4(sdf, sdf, sdf, 1.0);
}