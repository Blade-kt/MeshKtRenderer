#version 460 core

out layout(location = 0) vec4 COLOR_OUTPUT;
out layout(location = 1) vec4 SDF_OUTPUT;

void main() {
    COLOR_OUTPUT = vec4(0.0);
    SDF_OUTPUT = vec4(0.0);
}