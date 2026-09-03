package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.engine.descriptors.FontDescriptor
import me.blade.meshkt.renderer.engine.descriptors.IRectDescriptor
import me.blade.meshkt.renderer.engine.descriptors.IFontDescriptor
import me.blade.meshkt.renderer.engine.descriptors.RectDescriptor
import me.blade.meshkt.renderer.engine.font.buildGlyphMap
import me.blade.meshkt.renderer.engine.font.sdf
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.packVec2
import me.blade.meshkt.renderer.util.packVec3
import me.blade.meshkt.renderer.util.resourceText
import org.joml.Matrix4f
import java.awt.Font

class MeshInterfaceRenderer : IRenderContext {
    private val glyphMap = buildGlyphMap(
        Font("Arial", Font.PLAIN, 128)
    )

    private val sdfTexture = sdf(glyphMap.image)

    private val shader = createShader {
        compileSource(ShaderType.Vertex) { resourceText("/me/blade/mesh/shaders/interface.vsh") }
        compileSource(ShaderType.Fragment) { resourceText("/me/blade/mesh/shaders/interface.fsh") }
        link()

        val glyphs = glyphMap.charData.values
        storage.allocate("GlyphBuffer", glyphs.size * 16L, true) {
            glyphs.forEachIndexed { index, glyphData ->
                vec2(glyphData.u0, glyphData.v0)
                vec2(glyphData.u1, glyphData.v1)
                glyphData.index = index
            }
            upload()
        }

        uniforms {
            TextureSlot.entries.forEach { slot ->
                sampler("u_TEXTURE${slot.unitIndex}", slot)
            }
        }
    }
    private val storage = shader.storage

    private val instanceBuffer = storage.allocate("InstanceBuffer")
    private val textureAllocator = TextureAllocator(storage.allocate("TextureHandleBuffer"))

    /* Rect */
    private val rectDescriptor = RectDescriptor()

    private var rectInstanceCount = 0
    private val rectInstanceBuffer = storage.allocate("RectInstanceBuffer")

    /* Font */
    private val fontDescriptor = FontDescriptor()
    private var stringInstanceCount = 0
    private var charInstanceCount = 0
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

    override fun bindMatrix(type: MatrixType, matrix: Matrix4f) {
        when (type) {
            MatrixType.Projection -> projectionMatrixAllocator
            MatrixType.View -> viewMatrixAllocator
            MatrixType.Model -> modelMatrixAllocator
        }.bind(matrix)
    }

    override fun rect(block: IRectDescriptor.() -> Unit) {
        rectDescriptor.reset()
        block(rectDescriptor)
        rect(rectDescriptor)
    }

    override fun rect(descriptor: IRectDescriptor) {
        with(rectInstanceBuffer) {
            vec2(descriptor.pos1)
            vec2(descriptor.pos2)
            int(packColorARGB(descriptor.color))
            int(packedMatrices)
            int(textureAllocator.alloc(descriptor.texture))
            skip(4)
        }

        with(instanceBuffer) {
            int(packVec2(
                INSTANCE_BUFFER_BITS,
                INSTANCE_POINTER_BITS,
                RECT_BUFFER_INDEX,
                rectInstanceCount++
            ))
        }
    }

    override fun font(block: IFontDescriptor.() -> Unit) {
        fontDescriptor.reset()
        block(fontDescriptor)
        font(fontDescriptor)
    }

    override fun font(descriptor: IFontDescriptor) {
        val stringIndex = stringInstanceCount++
        with(stringInstanceBuffer) {
            int(packedMatrices)

            // TODO: on-fly glyph map generator
            // (and this actually should be per-char)
            int(textureAllocator.alloc(sdfTexture))

            float(descriptor.height)
            skip(4)
        }

        var xOffset = 0.0
        descriptor.text.forEach { char ->
            val glyph = glyphMap.charDataOf(char)

            with(instanceBuffer) {
                int(packVec2(
                    INSTANCE_BUFFER_BITS,
                    INSTANCE_POINTER_BITS,
                    CHAR_BUFFER_INDEX,
                    charInstanceCount++
                ))
            }

            with(charInstanceBuffer) {
                vec2(descriptor.pos.x + xOffset, descriptor.pos.y)
                int(stringIndex)
                int(glyph.index)
                xOffset += glyph.getCharWidth(descriptor.height)
            }
        }
    }

    override fun fontWidth(string: String, height: Double) = string.sumOf {
        glyphMap.charDataOf(it).getCharWidth(height)
    }

    fun use(block: IRenderContext.() -> Unit) {
        block(this)
    }

    fun flush() {
        projectionMatrixAllocator.flush()
        viewMatrixAllocator.flush()
        modelMatrixAllocator.flush()
        textureAllocator.flush()

        instanceBuffer.upload()
        rectInstanceBuffer.upload()
        stringInstanceBuffer.upload()
        charInstanceBuffer.upload()

        Mesh.boundTexture[TextureSlot.Slot0] = sdfTexture

        Mesh.boundShader = shader
        Mesh.render(rectInstanceCount + charInstanceCount)

        instanceBuffer.reset()

        rectInstanceCount = 0
        rectInstanceBuffer.reset()

        stringInstanceCount = 0
        charInstanceCount = 0
        stringInstanceBuffer.reset()
        charInstanceBuffer.reset()
    }

    fun fence() {
        projectionMatrixAllocator.reset()
        viewMatrixAllocator.reset()
        modelMatrixAllocator.reset()
        textureAllocator.reset()
    }

    companion object {
        private const val INSTANCE_BUFFER_BITS = 4
        private const val INSTANCE_POINTER_BITS = 28
        private const val RECT_BUFFER_INDEX = 0
        private const val CHAR_BUFFER_INDEX = 1
    }
}