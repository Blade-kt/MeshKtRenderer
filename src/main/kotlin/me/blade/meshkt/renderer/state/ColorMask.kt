package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL46C.*
import java.nio.ByteBuffer

data class ColorMask(
    val r: Boolean,
    val g: Boolean,
    val b: Boolean,
    val a: Boolean,
) {
    fun apply() {
        glColorMask(r, g, b, a)
    }

    companion object {
        val ALL = ColorMask(r = true, g = true, b = true, a = true)
        val NONE = ColorMask(r = false, g = false, b = false, a = false)

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