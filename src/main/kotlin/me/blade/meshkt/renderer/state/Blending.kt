package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL46C.*

enum class BlendFactor(override val gl: Int): GLInt {
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
    OneMinusSrc1Alpha(GL_ONE_MINUS_SRC1_ALPHA);

    companion object {
        private val valueMap = hashMapOf<Int, BlendFactor>().apply {
            BlendFactor.entries.forEach { entry ->
                this[entry.gl] = entry
            }
        }

        fun get(pname: Int) = valueMap[glGetInteger(pname)]!!
    }
}

enum class BlendEquationMode(override val gl: Int): GLInt {
    FuncAdd(GL_FUNC_ADD),
    FuncSubtract(GL_FUNC_SUBTRACT),
    FuncReverseSubtract(GL_FUNC_REVERSE_SUBTRACT),
    Min(GL_MIN),
    Max(GL_MAX);

    companion object {
        private val valueMap = hashMapOf<Int, BlendEquationMode>().apply {
            BlendEquationMode.entries.forEach { entry ->
                this[entry.gl] = entry
            }
        }

        fun get(pname: Int) = valueMap[glGetInteger(pname)]!!
    }
}

data class BlendFunc(
    val srcRgb: BlendFactor,
    val dstRgb: BlendFactor,
    val srcAlpha: BlendFactor,
    val dstAlpha: BlendFactor
) {
    constructor(src: BlendFactor, dst: BlendFactor) : this(src, dst, src, dst)

    fun apply() {
        glBlendFuncSeparate(srcRgb.gl, dstRgb.gl, srcAlpha.gl, dstAlpha.gl)
    }

    companion object {
        fun fromGL() = BlendFunc(
            BlendFactor.get(GL_BLEND_SRC_RGB),
            BlendFactor.get(GL_BLEND_DST_RGB),
            BlendFactor.get(GL_BLEND_SRC_ALPHA),
            BlendFactor.get(GL_BLEND_DST_ALPHA)
        )

        val default = BlendFunc(
            BlendFactor.SrcAlpha,
            BlendFactor.OneMinusSrcAlpha,
            BlendFactor.One,
            BlendFactor.OneMinusSrcAlpha
        )
    }
}

data class BlendEquation(
    val modeRgb: BlendEquationMode,
    val modeAlpha: BlendEquationMode
) {
    constructor(mode: BlendEquationMode) : this(mode, mode)

    fun apply() {
        glBlendEquationSeparate(modeRgb.gl, modeAlpha.gl)
    }

    companion object {
        fun fromGL() = BlendEquation(
            BlendEquationMode.get(GL_BLEND_EQUATION_RGB),
            BlendEquationMode.get(GL_BLEND_EQUATION_ALPHA)
        )
    }
}