package me.blade.meshkt.renderer.font

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import kotlin.math.sqrt

const val SDF_DOWNSCALE = 8
const val SDF_SCAN = 16
private val scope = CoroutineScope(Dispatchers.Default)

fun sdf(src: BufferedImage): BufferedImage {
    val downscale = SDF_DOWNSCALE
    val dst = BufferedImage(src.width / downscale, src.height / downscale, BufferedImage.TYPE_BYTE_GRAY)
    val raster = dst.raster

    fun isWhite(x: Int, y: Int): Boolean {
        if (x !in 0 until src.width || y !in 0 until src.height) return false
        return (src.raster.getSample(x, y, 0)) >= 128
    }

    fun distanceSq(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }

    val jobs = (0 until dst.width).map { x ->
        scope.async {
            repeat(dst.height) { y ->
                val srcX = x * downscale
                val srcY = y * downscale

                val boundaryPixelsNearby = arrayListOf<Pair<Int, Int>>()
                val scanDistance = SDF_SCAN

                repeat(scanDistance * 2) { xOffset ->
                    repeat(scanDistance * 2) { yOffset ->
                        val nx = srcX + xOffset - scanDistance
                        val ny = srcY + yOffset - scanDistance

                        val neighbours = listOf(
                            isWhite(nx + 0, ny - 1), // center top

                            isWhite(nx - 1, ny + 0), // left center
                            isWhite(nx + 1, ny + 0), // right center

                            isWhite(nx + 0, ny + 1), // center bottom
                        )

                        if (isWhite(nx, ny) && neighbours.any { !it }) {
                            boundaryPixelsNearby.add(Pair(nx, ny))
                        }
                    }
                }

                val insideSign = if (isWhite(srcX, srcY)) 1.0 else -1.0

                val distanceSq = boundaryPixelsNearby.minOfOrNull { (bx, by) ->
                    distanceSq(bx, by, srcX, srcY).toDouble().div(downscale)
                } ?: (scanDistance * scanDistance).toDouble()

                val distance = sqrt(distanceSq)
                val normalizedDistance = distance / scanDistance
                val brightness = 0.5 + normalizedDistance * insideSign

                raster.setSample(x, y, 0, (255 * brightness).toInt().coerceIn(0..255))
            }
        }
    }

    runBlocking {
        jobs.awaitAll()
    }

    return dst
}