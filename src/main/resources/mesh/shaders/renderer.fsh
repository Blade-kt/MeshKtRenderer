#version 460 core

in float S_BUFFER_INDEX;
in vec4 S_VERTEX_COLOR;

out vec4 COLOR_ATTACHMENT0;

void main() {
    int BUFFER_INDEX = int(S_BUFFER_INDEX);

    if (BUFFER_INDEX == 0) {
        COLOR_ATTACHMENT0 = S_VERTEX_COLOR;
    }
}