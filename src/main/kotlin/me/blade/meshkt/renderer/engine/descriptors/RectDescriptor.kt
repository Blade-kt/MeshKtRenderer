package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.util.vec.Vec2
import java.awt.Color

interface IRectDescriptor {
    var pos1: Vec2
    var pos2: Vec2

    var colorLeftTop: Color
    var colorRightTop: Color
    var colorRightBottom: Color
    var colorLeftBottom: Color

    var roundRadiusLeftTop: Double
    var roundRadiusRightTop: Double
    var roundRadiusRightBottom: Double
    var roundRadiusLeftBottom: Double

    var texture: Texture?
    var textureUV0: Vec2
    var textureUV1: Vec2

    fun color(color: Color)
    fun colorH(colorL: Color, colorR: Color)
    fun colorV(colorT: Color, colorB: Color)

    fun round(radius: Double)
    fun roundH(radiusL: Double, radiusR: Double)
    fun roundV(radiusT: Double, radiusB: Double)
}

class RectDescriptor : IRectDescriptor {
    override var pos1 = Vec2.ZERO
    override var pos2 = Vec2.ZERO

    override var colorLeftTop = Color.WHITE!!
    override var colorRightTop = Color.WHITE!!
    override var colorRightBottom = Color.WHITE!!
    override var colorLeftBottom = Color.WHITE!!

    override var roundRadiusLeftTop = 0.0
    override var roundRadiusRightTop = 0.0
    override var roundRadiusRightBottom = 0.0
    override var roundRadiusLeftBottom = 0.0

    override var texture: Texture? = null
    override var textureUV0 = Vec2.ZERO
    override var textureUV1 = Vec2.ONE

    override fun color(color: Color) {
        colorLeftTop = color
        colorRightTop = color
        colorRightBottom = color
        colorLeftBottom = color
    }

    override fun colorH(colorL: Color, colorR: Color) {
        colorLeftTop = colorL
        colorRightTop = colorR
        colorRightBottom = colorR
        colorLeftBottom = colorL
    }

    override fun colorV(colorT: Color, colorB: Color) {
        colorLeftTop = colorT
        colorRightTop = colorT
        colorRightBottom = colorB
        colorLeftBottom = colorB
    }

    override fun round(radius: Double) {
        roundRadiusLeftTop = radius
        roundRadiusRightTop = radius
        roundRadiusRightBottom = radius
        roundRadiusLeftBottom = radius
    }

    override fun roundH(radiusL: Double, radiusR: Double) {
        roundRadiusLeftTop = radiusL
        roundRadiusRightTop = radiusR
        roundRadiusRightBottom = radiusR
        roundRadiusLeftBottom = radiusL
    }

    override fun roundV(radiusT: Double, radiusB: Double) {
        roundRadiusLeftTop = radiusT
        roundRadiusRightTop = radiusT
        roundRadiusRightBottom = radiusB
        roundRadiusLeftBottom = radiusB
    }

    fun reset() {
        pos1 = Vec2.ZERO
        pos2 = Vec2.ZERO

        color(Color.WHITE)
        round(0.0)

        texture = null
        textureUV0 = Vec2.ZERO
        textureUV1 = Vec2.ONE
    }
}