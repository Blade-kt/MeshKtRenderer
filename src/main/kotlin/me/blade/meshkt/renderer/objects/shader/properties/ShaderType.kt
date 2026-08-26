package me.blade.meshkt.renderer.objects.shader.properties

import org.lwjgl.opengl.GL45C.*

enum class ShaderType(val gl: Int) {
    Vertex(GL_VERTEX_SHADER),
    Fragment(GL_FRAGMENT_SHADER),

    Geometry(GL_GEOMETRY_SHADER),
    TesselationControl(GL_TESS_CONTROL_SHADER),
    TesselationEvaluation(GL_TESS_EVALUATION_SHADER),

    Compute(GL_COMPUTE_SHADER);
}