package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.engine.descriptors.IRectDescriptor
import me.blade.meshkt.renderer.engine.descriptors.ITextDescriptor
import me.blade.meshkt.renderer.engine.descriptors.TextDescriptor
import org.joml.Matrix4f
import java.awt.Font

interface IRenderContext {
    fun getFont(name: String, styleBitmask: Int = Font.PLAIN) =
        Font(name, styleBitmask, 67 /* we actually override this anyway */)

    fun bindMatrix(type: MatrixType, matrix: Matrix4f)

    fun createRectDescriptor(block: IRectDescriptor.() -> Unit): IRectDescriptor
    fun rect(block: IRectDescriptor.() -> Unit)
    fun rect(descriptor: IRectDescriptor)

    fun createTextDescriptor(block: ITextDescriptor.() -> Unit): ITextDescriptor
    fun text(block: ITextDescriptor.() -> Unit)
    fun text(descriptor: ITextDescriptor)

    fun fontWidth(block: ITextDescriptor.() -> Unit): Double
    fun fontWidth(descriptor: ITextDescriptor): Double
}