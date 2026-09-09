package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.engine.allocators.Color4Allocator
import me.blade.meshkt.renderer.engine.allocators.FontAllocator
import me.blade.meshkt.renderer.engine.allocators.MatrixAllocator
import me.blade.meshkt.renderer.engine.allocators.ScissorStack
import me.blade.meshkt.renderer.engine.allocators.TextureAllocator
import me.blade.meshkt.renderer.engine.descriptors.TextDescriptor
import me.blade.meshkt.renderer.engine.descriptors.IRectDescriptor
import me.blade.meshkt.renderer.engine.descriptors.ITextDescriptor
import me.blade.meshkt.renderer.engine.descriptors.RectDescriptor
import me.blade.meshkt.renderer.engine.descriptors.ScissorData
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.util.Quad
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.packVec2
import me.blade.meshkt.renderer.util.packVec3
import me.blade.meshkt.renderer.util.packVec4
import me.blade.meshkt.renderer.util.resourceText
import org.joml.Matrix4f
import java.awt.Font
import kotlin.math.min
import kotlin.math.roundToInt

object MeshUI {
    private const val INSTANCE_BUFFER_BITS = 4
    private const val INSTANCE_POINTER_BITS = 28
    private const val RECT_BUFFER_INDEX = 0
    private const val CHAR_BUFFER_INDEX = 1

    private val shader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/ui.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/ui.fsh"))
        link()
    }

    private val storage = shader.storage

    private val scissorStack = ScissorStack(storage.allocate("ScissorDataBuffer"))
    private val scissorIndexBuffer = storage.allocate("ScissorIndexBuffer")
    private val textureAllocator = TextureAllocator(storage.allocate("TextureHandleBuffer"))

    private val instanceBuffer = storage.allocate("InstanceBuffer")
    private var rectInstanceCount = 0
    private var charInstanceCount = 0

    /* Rect */
    private val rectDescriptor = RectDescriptor()
    private val color4Allocator = Color4Allocator(storage.allocate("ColorBuffer"))
    private val rectInstanceBuffer = storage.allocate("RectInstanceBuffer")

    /* Font */
    var defaultFont = Font("SansSerif", Font.PLAIN, 12)
    var defaultTextHeight = 20.0
    private val textDescriptor = TextDescriptor()
    private var stringInstanceCount = 0

    private val glyphAllocator = FontAllocator(storage.allocate("GlyphBuffer"))
    private val stringInstanceBuffer = storage.allocate("StringInstanceBuffer")
    private val charInstanceBuffer = storage.allocate("CharInstanceBuffer")

    /* Matrices */
    private val projectionMatrixAllocator = MatrixAllocator(storage.allocate("ProjectionMatrixBuffer"), 4)
    private val viewMatrixAllocator = MatrixAllocator(storage.allocate("ViewMatrixBuffer"), 20)
    private val modelMatrixAllocator = MatrixAllocator(storage.allocate("ModelMatrixBuffer"), 8)
    private val packedMatrices get() = packVec3(
        projectionMatrixAllocator.bits,
        viewMatrixAllocator.bits,
        modelMatrixAllocator.bits,
        projectionMatrixAllocator.bound,
        viewMatrixAllocator.bound,
        modelMatrixAllocator.bound,
    )

    /**
     * Note that the matrix count is limited per frame:
     *
     * Projection - 16
     *
     * View - 1 048 576
     *
     * Model - 256
     */
    fun bindMatrix(type: MatrixType, matrix: Matrix4f) {
        when (type) {
            MatrixType.Projection -> projectionMatrixAllocator
            MatrixType.View -> viewMatrixAllocator
            MatrixType.Model -> modelMatrixAllocator
        }.bind(matrix)
    }

    fun createRectDescriptor(block: IRectDescriptor.() -> Unit) =
        RectDescriptor().apply(block)

    fun rect(block: IRectDescriptor.() -> Unit) {
        rectDescriptor.reset()
        block(rectDescriptor)
        rect(rectDescriptor)
    }

    fun rect(descriptor: IRectDescriptor) {
        with(rectInstanceBuffer) {
            vec2(descriptor.pos1)
            vec2(descriptor.pos2)
            int(color4Allocator.alloc(Quad(
                descriptor.colorLeftTop,
                descriptor.colorRightTop,
                descriptor.colorRightBottom,
                descriptor.colorLeftBottom
            )))
            int(descriptor.packRoundRadius())
            int(packedMatrices)
            int(textureAllocator.alloc(descriptor.texture))
        }

        scissorIndexBuffer.int(scissorStack.activeScissorSlot)
        putInstancePointer(RECT_BUFFER_INDEX)
    }

    fun createTextDescriptor(block: ITextDescriptor.() -> Unit) =
        TextDescriptor().apply(block)

    fun text(block: ITextDescriptor.() -> Unit) {
        textDescriptor.reset()
        block(textDescriptor)
        text(textDescriptor)
    }

    fun text(descriptor: ITextDescriptor) {
        val height = descriptor.height ?: defaultTextHeight
        val content = descriptor.content ?: error("ITextDescriptor.content is null")
        val pos = descriptor.pos ?: error("ITextDescriptor.pos is null")

        val font = descriptor.font ?: defaultFont
        val glyphMap = glyphAllocator.alloc(font)

        val stringIndex = stringInstanceCount++
        with(stringInstanceBuffer) {
            int(packColorARGB(descriptor.color))
            int(packedMatrices)
            // TODO: on-fly glyph map generator for unlimited character support
            // (and this actually should be per-char)
            int(textureAllocator.alloc(glyphMap.texture))
            float(height)
        }

        var xOffset = 0.0
        content.forEach { char ->
            val glyph = glyphMap.charDataOf(char)

            scissorIndexBuffer.int(scissorStack.activeScissorSlot)
            putInstancePointer(CHAR_BUFFER_INDEX)

            with(charInstanceBuffer) {
                vec2(pos.x + xOffset, pos.y)
                int(stringIndex)
                int(glyph.index)
                xOffset += glyph.getCharWidth(height)
            }
        }
    }

    private fun putInstancePointer(bufferIndex: Int) {
        with(instanceBuffer) {
            int(packVec2(
                INSTANCE_BUFFER_BITS,
                INSTANCE_POINTER_BITS,
                bufferIndex,
                when (bufferIndex) {
                    RECT_BUFFER_INDEX -> {
                        rectInstanceCount++
                    }
                    CHAR_BUFFER_INDEX -> {
                        charInstanceCount++
                    }
                    else -> error("Invalid instance")
                }
            ))
        }
    }

    fun fontWidth(block: ITextDescriptor.() -> Unit): Double {
        textDescriptor.reset()
        block(textDescriptor)
        return fontWidth(textDescriptor)
    }

    fun fontWidth(descriptor: ITextDescriptor): Double {
        val height = descriptor.height ?: defaultTextHeight
        val content = descriptor.content ?: error("ITextDescriptor.content is null")

        val font = descriptor.font ?: defaultFont
        val glyphMap = glyphAllocator.alloc(font)

        return content.sumOf {
            glyphMap.charDataOf(it).getCharWidth(height)
        }
    }

    fun pushScissor(scissorData: ScissorData, clamp: Boolean = true) {
        scissorStack.push(scissorData, clamp)
    }

    fun popScissor() {
        scissorStack.pop()
    }

    fun flush() {
        projectionMatrixAllocator.flush()
        viewMatrixAllocator.flush()
        modelMatrixAllocator.flush()

        color4Allocator.flush()
        textureAllocator.flush()
        glyphAllocator.flush()

        scissorStack.flush()
        scissorIndexBuffer.upload()

        instanceBuffer.upload()
        rectInstanceBuffer.upload()
        stringInstanceBuffer.upload()
        charInstanceBuffer.upload()

        Mesh.boundShader = shader
        shader.uniforms.float("u_FONT_DIM_SIZE", 2048.0)
        Mesh.render(rectInstanceCount + charInstanceCount)

        instanceBuffer.reset()
        scissorIndexBuffer.reset()

        rectInstanceBuffer.reset()
        stringInstanceBuffer.reset()
        charInstanceBuffer.reset()

        rectInstanceCount = 0
        stringInstanceCount = 0
        charInstanceCount = 0
    }

    fun reset() {
        projectionMatrixAllocator.reset()
        viewMatrixAllocator.reset()
        modelMatrixAllocator.reset()
        textureAllocator.reset()
        color4Allocator.reset()
        scissorStack.reset()
    }

    private fun IRectDescriptor.packRoundRadius(): Int {
        val maxRound = min((pos2.x - pos1.x), (pos2.y - pos1.y)) * 0.5

        return packVec4(
            8, 8, 8, 8,
            roundRadiusRightBottom.coerceAtMost(maxRound).times(2).roundToInt().coerceIn(0..255),
            roundRadiusRightTop.coerceAtMost(maxRound).times(2).roundToInt().coerceIn(0..255),
            roundRadiusLeftBottom.coerceAtMost(maxRound).times(2).roundToInt().coerceIn(0..255),
            roundRadiusLeftTop.coerceAtMost(maxRound).times(2).roundToInt().coerceIn(0..255),
        )
    }
}