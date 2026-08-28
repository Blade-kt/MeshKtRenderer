package me.blade.meshkt.renderer.font

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.image.BufferedImage
import kotlin.math.pow
import kotlin.math.sqrt

object SDFGen {
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

    fun generateSDFGlyphMap(
        font: Font,
    ): BufferedImage {
        return sdf(renderGlyphMap(font.deriveFont(128f)))
    }

    fun sdf(src: BufferedImage): BufferedImage {
        val dst = BufferedImage(src.width, src.height, BufferedImage.TYPE_BYTE_GRAY)
        val raster = dst.raster

        fun isWhite(x: Int, y: Int): Boolean {
            if (x !in 0 until src.width || y !in 0 until src.height) return false
            return (src.raster.getSample(x, y, 0)) >= 128
        }

        fun distanceSq(x1: Int, y1: Int, x2: Int, y2: Int): Double {
            val dx = x1 - x2
            val dy = y1 - y2
            return dx * dx + dy * dy.toDouble()
        }

        val totalIterations = src.width * src.height
        var iteration = 0
        var lastPrintTime = System.currentTimeMillis()

        val scope = CoroutineScope(Dispatchers.Default)
        val jobs = (0 until src.width).map { x ->
            scope.async {
                repeat(src.height) { y ->
                    val boundaryPixelsNearby = arrayListOf<Pair<Int, Int>>()
                    val scanDistance = 16

                    repeat(scanDistance * 2) { xOffset ->
                        repeat(scanDistance * 2) { yOffset ->
                            val nx = x + xOffset - scanDistance
                            val ny = y + yOffset - scanDistance

                            val neighbours = listOf(
                                //isWhite(nx - 1, ny - 1), // left top
                                isWhite(nx + 0, ny - 1), // center top
                                //isWhite(nx + 1, ny - 1), // right top

                                isWhite(nx - 1, ny + 0), // left center
                                isWhite(nx + 1, ny + 0), // right center

                                //isWhite(nx - 1, ny + 1), // left bottom
                                isWhite(nx + 0, ny + 1), // center bottom
                                //isWhite(nx + 1, ny + 1)  // right bottom
                            )

                            if (isWhite(nx, ny) && neighbours.any { !it }) {
                                boundaryPixelsNearby.add(Pair(nx, ny))
                            }
                        }
                    }

                    val insideSign = if (isWhite(x, y)) 1.0 else -1.0

                    val distanceSq = boundaryPixelsNearby.minOfOrNull { (bx, by) ->
                        distanceSq(bx, by, x, y)
                    } ?: (scanDistance * scanDistance).toDouble()

                    val distance = sqrt(distanceSq)
                    val normalizedDistance = distance / scanDistance
                    val brightness = 0.5 + normalizedDistance * insideSign

                    raster.setSample(x, y, 0, (255 * brightness).toInt().coerceIn(0..255))

                    val perc = iteration.toDouble().div(totalIterations).times(10000).toInt().times(0.01)
                    iteration++
                    if (System.currentTimeMillis() - lastPrintTime > 1000) {
                        lastPrintTime = System.currentTimeMillis()
                        println("SDF ${perc}%")
                    }
                }
            }
        }

        runBlocking {
            jobs.awaitAll()
        }

        // blur to make it smoother (to fix the shit)
        val blurred = BufferedImage(src.width, src.height, BufferedImage.TYPE_BYTE_GRAY)
        repeat(blurred.width) { x ->
            repeat(blurred.height) { y ->
                fun sample(x: Int, y: Int): Int {
                    if (x !in 0 until dst.width || y !in 0 until dst.height) return 0
                    return raster.getSample(x, y, 0)
                }

                val blurRadius = 4
                val avg = (0 until blurRadius * 2).flatMap { xoffs ->
                    (0 until blurRadius * 2).map { yoffs ->
                        val xb = x + xoffs - blurRadius
                        val yb = y + yoffs - blurRadius
                        sample(xb, yb)
                    }
                }.sum().toDouble() / (blurRadius * 2).toDouble().pow(2)

                blurred.raster.setSample(x, y, 0, avg.toInt().coerceIn(0..255))
            }
        }

        return blurred
    }

    fun renderGlyphMap(font: Font): BufferedImage {
        val metrics = getFontMetrics(font)

        val chars = supportedCharacters.mapNotNull {
            getCharacterImage(metrics, it)
        }

        val renderPos = getRenderPositions(chars, metrics.height)
        val image = BufferedImage(renderPos.second, renderPos.second, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = image.createGraphics()

        chars.forEachIndexed { index, charImage ->
            val (x, y) = renderPos.first[index]
            graphics.drawImage(
                charImage,
                x, y, x + charImage.width,  y + charImage.height,
                0, 0, charImage.width, charImage.height,
                null
            )
        }

        graphics.dispose()
        return image
    }

    private fun getRenderPositions(chars: List<BufferedImage>, rowHeight: Int, minSize: Int = 128): Pair<ArrayList<Pair<Int, Int>>, Int> {
        val space = 8

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

    private fun getCharacterImage(metrics: FontMetrics, char: Char): BufferedImage? {
        val width = metrics.charWidth(char)
        if (width <= 0 || !metrics.font.canDisplay(char)) return null

        val image = BufferedImage(width, metrics.height, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = image.createGraphics()

        graphics.font = metrics.font
        graphics.color = Color.WHITE
        graphics.drawString(char.toString(), 0, metrics.ascent)

        graphics.dispose()
        return image
    }

    private fun getFontMetrics(font: Font): FontMetrics {
        val image = BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = image.createGraphics()

        graphics.font = font
        val metrics = graphics.fontMetrics
        graphics.dispose()

        return metrics
    }
}

private fun Int.isPoT() =
    this.let { n -> n > 0 && (n and (n - 1)) == 0 }