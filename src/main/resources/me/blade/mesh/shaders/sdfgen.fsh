#version 460 core

uniform sampler2D u_FONT_TEXTURE;

uniform vec2 u_SRC_SIZE;
uniform vec2 u_DST_SIZE;

uniform int u_SDF_DOWNSCALE;
uniform int u_SDF_SCAN;

in vec2 s_UV;

out vec4 COLOR_ATTACHMENT0;

bool isWhite(vec2 srcPixel) {
    return texture(u_FONT_TEXTURE, srcPixel / u_SRC_SIZE).r > 0.5;
}

void main() {
    float srcX = s_UV.x * u_SRC_SIZE.x;
    float srcY = s_UV.y * u_SRC_SIZE.y;

    float distanceSq = u_SDF_SCAN * u_SDF_SCAN;

    for (int offsetX = -u_SDF_SCAN; offsetX < u_SDF_SCAN; offsetX++) {
        for (int offsetY = -u_SDF_SCAN; offsetY < u_SDF_SCAN; offsetY++) {
            float readX = srcX + offsetX;
            float readY = srcY + offsetY;

            bool centerTopWhite = isWhite(vec2(readX + 0, readY - 1));
            bool centerLeftWhite = isWhite(vec2(readX - 1, readY + 0));
            bool centerRightWhite = isWhite(vec2(readX + 1, readY + 0));
            bool centerBottomWhite = isWhite(vec2(readX + 0, readY + 1));
            bool hasBlackNeighbor = !centerTopWhite || !centerLeftWhite || !centerRightWhite || !centerBottomWhite;
            bool isCorner = isWhite(vec2(readX, readY)) && hasBlackNeighbor;

            if (isCorner) {
                vec2 diff = vec2(srcX - readX, srcY - readY);
                float currDst =  diff.x * diff.x + diff.y * diff.y;
                if (currDst < distanceSq) {
                    distanceSq = currDst;
                }
            }
        }
    }

    float insideSign = 1.0;
    if (!isWhite(vec2(srcX, srcY))) insideSign = -1.0;

    float normalizedDistance = sqrt(distanceSq) / (u_SDF_SCAN * sqrt(2.0));
    float sdf = 0.5 + normalizedDistance * insideSign;

    COLOR_ATTACHMENT0 = vec4(sdf, sdf, sdf, 1.0);
}