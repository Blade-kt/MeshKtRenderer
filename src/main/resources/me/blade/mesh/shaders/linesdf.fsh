#version 460 core

flat in vec2 s_RECT_POS1;
flat in vec2 s_RECT_POS2;
in vec2 s_FRAG_COORD;
in vec4 s_COLOR;
in float s_WIDTH;

out layout(location = 0) vec4 COLOR_OUTPUT;
out layout(location = 1) vec4 SDF_OUTPUT;

void main() {
    vec2 lineVec = s_RECT_POS2 - s_RECT_POS1;
    vec2 fragToStart = s_FRAG_COORD - s_RECT_POS1;

    float lineLengthSq = dot(lineVec, lineVec);

    float t = 0.0;
    if (lineLengthSq > 0.001) {
        t = clamp(dot(fragToStart, lineVec) / lineLengthSq, 0.0, 1.0);
    }

    vec2 closestPoint = s_RECT_POS1 + t * lineVec;

    float distance = length(s_FRAG_COORD - closestPoint);
    float sdf = 1.0 - distance / s_WIDTH;
    float colorSDF = smoothstep(0.2, 0.8, sdf);

    COLOR_OUTPUT = s_COLOR * vec4(1.0, 1.0, 1.0, colorSDF);
    SDF_OUTPUT = vec4(sdf, sdf, sdf, 1.0);
}