package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL11C.GL_DST_ALPHA
import org.lwjgl.opengl.GL11C.GL_DST_COLOR
import org.lwjgl.opengl.GL11C.GL_ONE
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_DST_ALPHA
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_DST_COLOR
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_COLOR
import org.lwjgl.opengl.GL11C.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11C.GL_SRC_ALPHA_SATURATE
import org.lwjgl.opengl.GL11C.GL_SRC_COLOR
import org.lwjgl.opengl.GL11C.GL_ZERO
import org.lwjgl.opengl.GL14C.GL_CONSTANT_ALPHA
import org.lwjgl.opengl.GL14C.GL_CONSTANT_COLOR
import org.lwjgl.opengl.GL14C.GL_FUNC_ADD
import org.lwjgl.opengl.GL14C.GL_FUNC_REVERSE_SUBTRACT
import org.lwjgl.opengl.GL14C.GL_FUNC_SUBTRACT
import org.lwjgl.opengl.GL14C.GL_MAX
import org.lwjgl.opengl.GL14C.GL_MIN
import org.lwjgl.opengl.GL14C.GL_ONE_MINUS_CONSTANT_ALPHA
import org.lwjgl.opengl.GL14C.GL_ONE_MINUS_CONSTANT_COLOR
import org.lwjgl.opengl.GL15C.GL_SRC1_ALPHA
import org.lwjgl.opengl.GL33C.GL_ONE_MINUS_SRC1_ALPHA
import org.lwjgl.opengl.GL33C.GL_ONE_MINUS_SRC1_COLOR
import org.lwjgl.opengl.GL33C.GL_SRC1_COLOR

enum class BlendFactor(val gl: Int) {
    Zero(GL_ZERO),
    One(GL_ONE),
    SrcColor(GL_SRC_COLOR),
    OneMinusSrcColor(GL_ONE_MINUS_SRC_COLOR),
    DstColor(GL_DST_COLOR),
    OneMinusDstColor(GL_ONE_MINUS_DST_COLOR),
    SrcAlpha(GL_SRC_ALPHA),
    OneMinusSrcAlpha(GL_ONE_MINUS_SRC_ALPHA),
    DstAlpha(GL_DST_ALPHA),
    OneMinusDstAlpha(GL_ONE_MINUS_DST_ALPHA),
    ConstantColor(GL_CONSTANT_COLOR),
    OneMinusConstantColor(GL_ONE_MINUS_CONSTANT_COLOR),
    ConstantAlpha(GL_CONSTANT_ALPHA),
    OneMinusConstantAlpha(GL_ONE_MINUS_CONSTANT_ALPHA),
    SrcAlphaSaturate(GL_SRC_ALPHA_SATURATE),
    Src1Color(GL_SRC1_COLOR),
    OneMinusSrc1Color(GL_ONE_MINUS_SRC1_COLOR),
    Src1Alpha(GL_SRC1_ALPHA),
    OneMinusSrc1Alpha(GL_ONE_MINUS_SRC1_ALPHA)
}

enum class BlendEquationMode(val gl: Int) {
    FuncAdd(GL_FUNC_ADD),
    FuncSubtract(GL_FUNC_SUBTRACT),
    FuncReverseSubtract(GL_FUNC_REVERSE_SUBTRACT),
    Min(GL_MIN),
    Max(GL_MAX)
}

data class BlendFunc(
    val srcRgb: BlendFactor,
    val dstRgb: BlendFactor,
    val srcAlpha: BlendFactor,
    val dstAlpha: BlendFactor
) {
    constructor(src: BlendFactor, dst: BlendFactor) : this(src, dst, src, dst)
}

data class BlendEquation(
    val modeRgb: BlendEquationMode,
    val modeAlpha: BlendEquationMode
) {
    constructor(mode: BlendEquationMode) : this(mode, mode)
}