package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.font.buildGlyphMap
import me.blade.meshkt.renderer.font.sdf
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.packVec2
import me.blade.meshkt.renderer.util.packVec3
import me.blade.meshkt.renderer.util.resourceText
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11C.*
import java.awt.Color
import java.awt.Font

object MeshRenderer {
    private val glyphMap = buildGlyphMap(
        Font("Arial", Font.PLAIN, 128)
    )

    private val sdfTexture = sdf(glyphMap.image)

    private val shader = createShader {
        compileSource(ShaderType.Vertex) { resourceText("/mesh/shaders/renderer.vsh") }
        compileSource(ShaderType.Fragment) { resourceText("/mesh/shaders/renderer.fsh") }
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

    private const val INSTANCE_BUFFER_BITS = 4
    private const val INSTANCE_POINTER_BITS = 28
    private val instanceBuffer = storage.allocate("InstanceBuffer")

    /* Rect */
    private const val RECT_BUFFER_INDEX = 0
    private var rectInstanceCount = 0
    private val rectInstanceBuffer = storage.allocate("RectInstanceBuffer")

    /* Font */
    private const val CHAR_BUFFER_INDEX = 1
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

    fun bindMatrix(type: MatrixType, matrix: Matrix4f) {
        when (type) {
            MatrixType.Projection -> projectionMatrixAllocator
            MatrixType.View -> viewMatrixAllocator
            MatrixType.Model -> modelMatrixAllocator
        }.bind(matrix)
    }

    fun putRect(
        pos1x: Double, pos1y: Double,
        pos2x: Double, pos2y: Double,
        color: Color
    ) {
        with(rectInstanceBuffer) {
            vec2(pos1x, pos1y)
            vec2(pos2x, pos2y)
            int(packColorARGB(color))
            int(packedMatrices)
            int(0) // tex index
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

    fun putString(
        string: String,
        posX: Double, posY: Double, height: Double
    ) {
        // create string instance
        val stringIndex = stringInstanceCount++
        with(stringInstanceBuffer) {
            int(packedMatrices)
            float(height)
        }

        var xOffset = 0.0
        string.forEach { char ->
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
                vec2(posX + xOffset, posY)
                int(stringIndex)
                int(glyph.index)
                xOffset += glyph.getCharWidth(height)
            }
        }
    }

    fun stringWidth(string: String, height: Double) = string.sumOf {
        glyphMap.charDataOf(it).getCharWidth(height)
    }

    fun flush() {
        projectionMatrixAllocator.flush()
        viewMatrixAllocator.flush()
        modelMatrixAllocator.flush()

        instanceBuffer.upload()
        rectInstanceBuffer.upload()
        stringInstanceBuffer.upload()
        charInstanceBuffer.upload()

        Mesh.boundTexture[TextureSlot.Slot0] = sdfTexture
        Mesh.boundShader = shader

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        Mesh.render(shader, rectInstanceCount + charInstanceCount)

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
    }
}