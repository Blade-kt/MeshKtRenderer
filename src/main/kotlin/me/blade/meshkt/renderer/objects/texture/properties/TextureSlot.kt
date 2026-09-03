package me.blade.meshkt.renderer.objects.texture.properties

import me.blade.meshkt.renderer.state.GLInt
import org.lwjgl.opengl.GL13C.*

enum class TextureSlot(override val gl: Int) : GLInt {
    Slot0(GL_TEXTURE0),
    Slot1(GL_TEXTURE1),
    Slot2(GL_TEXTURE2),
    Slot3(GL_TEXTURE3),
    Slot4(GL_TEXTURE4),
    Slot5(GL_TEXTURE5),
    Slot6(GL_TEXTURE6),
    Slot7(GL_TEXTURE7),
    Slot8(GL_TEXTURE8),
    Slot9(GL_TEXTURE9),
    Slot10(GL_TEXTURE10),
    Slot11(GL_TEXTURE11),
    Slot12(GL_TEXTURE12),
    Slot13(GL_TEXTURE13),
    Slot14(GL_TEXTURE14),
    Slot15(GL_TEXTURE15);

    val unitIndex = gl - GL_TEXTURE0
}