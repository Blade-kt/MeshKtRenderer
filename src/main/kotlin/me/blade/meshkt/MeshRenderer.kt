package me.blade.meshkt

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.font.buildGlyphMap
import me.blade.meshkt.renderer.font.sdf
import me.blade.meshkt.renderer.objects.createBuffer
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureMinFilter
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelFormat
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelType
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.packVec2
import me.blade.meshkt.renderer.util.packVec3
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.glfwGetTime
import java.awt.Color
import java.awt.Font
import kotlin.collections.associateWith
import kotlin.math.sin

object MeshRenderer {
    val glyphMap = buildGlyphMap(
        Font("SansSerif", Font.PLAIN, 128)
    )

    val sdfTexture = createTexture {
        val image = sdf(glyphMap.image)
        val buffer = createBuffer {
            repeat(image.height) { y ->
                repeat(image.width) { x ->
                    byte(image.raster.getSample(x, y, 0).toByte())
                }
            }
        }

        filtering {
            minFilter = TextureMinFilter.Linear
            magFilter = TextureMagFilter.Linear
        }

        storage {
            width = image.width
            height = image.height

            internalFormat = TextureInternalFormat.R8
            uploadPixelFormat = TexturePixelFormat.Red
            uploadPixelType = TexturePixelType.UnsignedByte

            allocate()
            upload(buffer.pointer)
        }
        buffer.free()
    }

    val shader = createShader {
        fun loadShaderText(path: String): String {
            val stream = javaClass.getResourceAsStream(path)!!
            return stream.bufferedReader(Charsets.UTF_8).readText()
        }

        compileSource(ShaderType.Vertex) {
            loadShaderText("/mesh/shaders/renderer.vsh")
        }

        compileSource(ShaderType.Fragment) {
            loadShaderText("/mesh/shaders/renderer.fsh")
        }

        link()
        allocateStorages(1024,
            "MatrixBuffer", "InstanceBuffer",
            "RectInstanceBuffer",
            "StringInstanceBuffer", "CharInstanceBuffer", "GlyphBuffer"
        )

        write("GlyphBuffer") {
            reset()
            glyphMap.charData.values.forEach { glyphData ->
                vec(glyphData.u0, glyphData.v0)
                vec(glyphData.u1, glyphData.v1)
            }
            upload()
        }

    }

    var frameCount = 0
    private val time get() = System.currentTimeMillis()
    var lastPrintTime = time

    fun frame() {
        frameCount++

        if (time - lastPrintTime > 1000) {
            println(frameCount)
            lastPrintTime = time
            frameCount = 0
        }

        shader.write("MatrixBuffer") {
            reset()

            // proj
            mat(Matrix4f().ortho(0f, MeshRendererExample.viewportWidth.toFloat(), MeshRendererExample.viewportHeight.toFloat(), 0f, -10000f, 10000f))
            // view
            mat(Matrix4f().identity())
            // model
            mat(Matrix4f().identity())

            upload()
        }

        shader.write("InstanceBuffer") {
            reset()

            // 4 bits for buffer index, 28 for instance index
            int(packVec2(4, 28, 1, 0)) // B
            int(packVec2(4, 28, 1, 1)) // l
            int(packVec2(4, 28, 1, 2)) // a
            int(packVec2(4, 28, 1, 3)) // d
            int(packVec2(4, 28, 1, 4)) // e

            upload()
        }

        val height = 10.0 + (sin(glfwGetTime()) * 0.5 + 0.5) * 400
        shader.write("StringInstanceBuffer") {
            reset()

            int(packVec3(4, 20, 8, 0, 1, 2)) // matrices
            float(height)

            upload()
        }

        shader.write("CharInstanceBuffer") {
            reset()

            var x = 0.0
            "Blade".forEach {
                vec(10.0 + x, 200.0) // position
                int(0) // string index
                int(glyphMap.charIndexMap[it]!!)
                x += glyphMap.charData[it]!!.getCharWidth(height)
            }

            upload()
        }

        Mesh.boundShader = shader
        Mesh.boundTexture[TextureSlot.Slot0] = sdfTexture
        Mesh.render(shader, 5)
    }
}