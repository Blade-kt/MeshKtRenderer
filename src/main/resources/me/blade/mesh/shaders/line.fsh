#version 460 core

flat in vec2 s_RECT_POS1;
flat in vec2 s_RECT_POS2;
in vec2 s_FRAG_COORD;
in vec4 s_COLOR;
in float s_WIDTH;

out layout(location = 0) vec4 COLOR_ATTACHMENT0;

void main() {
    vec2 lineVec = s_RECT_POS2 - s_RECT_POS1;
    vec2 fragToStart = s_FRAG_COORD - s_RECT_POS1;
    float lineLengthSq = dot(lineVec, lineVec);
    float t = (lineLengthSq < 0.001) ? 0.0 : clamp(dot(fragToStart, lineVec) / lineLengthSq, 0.0, 1.0);
    vec2 closestPoint = s_RECT_POS1 + t * lineVec;

    float sdf = 1.0 - length(s_FRAG_COORD - closestPoint) / s_WIDTH;
    float alpha = smoothstep(0.3, 0.7, sdf);

    COLOR_ATTACHMENT0 = s_COLOR * vec4(1.0, 1.0, 1.0, alpha);
}