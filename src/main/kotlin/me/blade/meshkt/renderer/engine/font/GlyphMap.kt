package me.blade.meshkt.renderer.engine.font

import me.blade.meshkt.renderer.objects.texture.Texture
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.image.BufferedImage
import kotlin.collections.forEach
import kotlin.math.roundToInt

const val FONT_SIZE = 512f

private val supportedCharacters = buildString {
    append(' ')
    listOf(
        0x0020..0x007F,  // ASCII
        0x00A0..0x00FF,  // Latin-1
        0x0100..0x017F,  // Latin-Ext-A
        0x0180..0x024F,  // Latin-Ext-B
        0x0400..0x052F,  // Cyrillic
    ).forEach { range ->
        range.forEach {
            append(it.toChar())
        }
    }
}

data class GlyphMap(
    val texture: Texture,
    val normalizedCenter: Double,
    val charData: Map<Char, GlyphData>
) {
    fun charDataOf(char: Char) =
        charData[char] ?: charData['?']!!
}

data class GlyphData(
    var u0: Double,
    var v0: Double,
    var u1: Double,
    var v1: Double,
    var index: Int = 0 // lateinit
) {
    fun getCharWidth(height: Double): Double {
        val aspectRatio = (u1 - u0) / (v1 - v0)
        return height * aspectRatio
    }
}

fun buildGlyphMap(fontIn: Font): GlyphMap {
    val font = fontIn.takeIf {
        it.size2D == FONT_SIZE
    } ?: fontIn.deriveFont(FONT_SIZE)

    val metrics = getFontMetrics(font)

    val charPairs = supportedCharacters.mapNotNull {
        getCharacterImage(metrics, it)
    }

    val chars = charPairs.map { it.second }
    val images = charPairs.map { it.first }
    val height = images.maxOf { it.height }

    val charMap = hashMapOf<Char, GlyphData>()
    val renderPos = getRenderPositions(images, height)
    val image = BufferedImage(renderPos.second, renderPos.second, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = image.createGraphics()

    images.forEachIndexed { index, charImage ->
        val (x, y) = renderPos.first[index]

        val pos1 = x to y
        val pos2 = (x + charImage.width) to (y + charImage.height)

        charMap[chars[index]] = GlyphData(
            pos1.first.toDouble() / image.width,
            pos1.second.toDouble() / image.height,
            pos2.first.toDouble() / image.width,
            pos2.second.toDouble() / image.height
        )

        graphics.drawImage(
            charImage,
            x, y, x + charImage.width, y + charImage.height,
            0, 0, charImage.width, charImage.height,
            null
        )
    }

    graphics.dispose()

    val center = (metrics.ascent + metrics.descent) * 0.5 / metrics.height
    val texture = sdf(image)
    return GlyphMap(texture,center, charMap)
}

private fun getRenderPositions(chars: List<BufferedImage>, rowHeight: Int, minSize: Int = 128): Pair<ArrayList<Pair<Int, Int>>, Int> {
    val space = 16

    var x = space
    var y = space
    val maxDim = minSize - space

    val positions = ArrayList<Pair<Int, Int>>()

    chars.forEach { charImage ->
        // break if any char doesn't fit render space
        if (charImage.width > minSize - space * 2) {
            return getRenderPositions(chars, rowHeight, minSize * 2)
        }

        // go to next line if no space in the row
        if (x + charImage.width > maxDim) {
            x = space
            y += space + rowHeight
        }

        // break if cant no rows left
        if (y + rowHeight > maxDim) {
            return getRenderPositions(chars, rowHeight, minSize * 2)
        }

        // 'render'
        positions.add(Pair(x, y))
        x += charImage.width + space
    }

    // passed, return it
    return positions to minSize
}

private fun getCharacterImage(metrics: FontMetrics, char: Char): Pair<BufferedImage, Char>? {
    val width = metrics.charWidth(char)
    if (width <= 0 || !metrics.font.canDisplay(char)) return null

    val image = BufferedImage(width, metrics.height - metrics.leading, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = image.createGraphics()

    val bounds = metrics.getMaxCharBounds(graphics)

    graphics.font = metrics.font
    graphics.color = Color.WHITE

    graphics.drawString(char.toString(), 0, metrics.ascent - metrics.leading)

    graphics.dispose()
    return image to char
}

private fun getFontMetrics(font: Font): FontMetrics {
    val image = BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = image.createGraphics()

    graphics.font = font
    val metrics = graphics.fontMetrics
    graphics.dispose()

    return metrics
}