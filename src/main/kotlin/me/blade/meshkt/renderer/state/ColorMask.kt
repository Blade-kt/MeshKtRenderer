package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL46C.*
import java.nio.ByteBuffer

data class ColorMask(
    val r: Boolean = true,
    val g: Boolean = true,
    val b: Boolean = true,
    val a: Boolean = true,
) {
    fun apply() {
        glColorMask(r, g, b, a)
    }

    companion object {
        private val buffer = ByteBuffer.allocateDirect(4)

        fun fromGL(): ColorMask {
            glGetBooleanv(GL_COLOR_WRITEMASK, buffer)
            return ColorMask(
                buffer[0].toInt() == GL_TRUE,
                buffer[1].toInt() == GL_TRUE,
                buffer[2].toInt() == GL_TRUE,
                buffer[3].toInt() == GL_TRUE
            )
        }
    }
}