#version 460 core

#define MAX_WIDTH 16
#define SMOOTHING_BIAS 2

flat in vec2 s_RECT_POS1;
flat in vec2 s_RECT_POS2;
in vec2 s_FRAG_COORD;
in vec4 s_COLOR;
in float s_WIDTH;

out layout(location = 0) vec4 COLOR_ATTACHMENT0;

float linearstep(float edge0, float edge1, float x) {
    return clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
}

void main() {
    vec2 lineVec = s_RECT_POS2 - s_RECT_POS1;
    vec2 fragToStart = s_FRAG_COORD - s_RECT_POS1;
    float lineLengthSq = dot(lineVec, lineVec);
    float t = (lineLengthSq < 0.001) ? 0.0 : clamp(dot(fragToStart, lineVec) / lineLengthSq, 0.0, 1.0);
    vec2 closestPoint = s_RECT_POS1 + t * lineVec;

    float sdf = 1.0 - length(s_FRAG_COORD - closestPoint) / s_WIDTH;
    float smoothness = 1.0 - linearstep(0.0, s_WIDTH + SMOOTHING_BIAS, s_WIDTH);
    float alpha = linearstep(-smoothness, smoothness, sdf * 2 - 1);

    COLOR_ATTACHMENT0 = s_COLOR * vec4(1.0, 1.0, 1.0, alpha);
}