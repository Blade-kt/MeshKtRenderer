package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.engine.descriptors.IRectDescriptor
import me.blade.meshkt.renderer.engine.descriptors.IFontDescriptor
import org.joml.Matrix4f

interface IRenderContext {
    fun bindMatrix(type: MatrixType, matrix: Matrix4f)

    fun rect(block: IRectDescriptor.() -> Unit)
    fun rect(descriptor: IRectDescriptor)

    fun font(block: IFontDescriptor.() -> Unit)
    fun font(descriptor: IFontDescriptor)

    fun fontWidth(string: String, height: Double): Double
}