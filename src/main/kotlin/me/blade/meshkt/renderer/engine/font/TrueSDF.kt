package me.blade.meshkt.renderer.engine.font

import me.blade.meshkt.renderer.util.vec.Vec2i
import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun fastSDF(image: BufferedImage): BufferedImage {
    fun WritableRaster.isWhite(x: Int, y: Int): Boolean {
        return getSample(x, y, 0) > 127
    }
    val edgePixels = (0 until image.width).flatMap { x ->
        (0 until image.height).map { y ->
            val current = image.raster.isWhite(x, y)

            val left    = if (x > 0)                    image.raster.isWhite(x - 1, y) else current
            val right   = if (x < image.width - 1)      image.raster.isWhite(x + 1, y) else current
            val up      = if (y > 0)                    image.raster.isWhite(x, y - 1) else current
            val down    = if (y < image.height - 1)     image.raster.isWhite(x, y + 1) else current

            if (current != left || current != right || current != up || current != down) {
                Vec2i.create(x, y)
            } else null
        }
    }.filterNotNull().toHashSet()

    val w = image.width
    val h = image.height

    // ------------------------------------------------------------------
    // Exact Euclidean distance transform (Felzenszwalb-Huttenlocher)
    // Operates on squared distances; input is a 1D array where 0 marks a
    // source pixel and Int.MAX_VALUE marks everything else.
    // ------------------------------------------------------------------
    fun edt1d(f: IntArray): IntArray {
        val n = f.size
        val d = IntArray(n)
        val v = IntArray(n)
        val z = DoubleArray(n + 1)
        var k = 0
        v[0] = 0
        z[0] = -1e20
        z[1] = 1e20
        for (q in 1 until n) {
            var s: Double
            while (true) {
                val fq = f[q].toDouble()
                val fv = f[v[k]].toDouble()
                s = ((fq + q.toDouble() * q) - (fv + v[k].toDouble() * v[k])) /
                        (2.0 * q - 2.0 * v[k])
                if (s <= z[k]) k-- else break
            }
            k++
            v[k] = q
            z[k] = s
            z[k + 1] = 1e20
        }
        k = 0
        for (q in 0 until n) {
            while (z[k + 1] < q) k++
            val dq = q - v[k]
            d[q] = dq * dq + f[v[k]]
        }
        return d
    }

    // ------------------------------------------------------------------
    // Build the squared EDT.
    // Pass 1: for each row, distance to nearest edge in that row.
    // Pass 2: for each column, distance to nearest edge in that column,
    //         combined with the row results — this yields the true 2D EDT.
    // ------------------------------------------------------------------
    val INF = Int.MAX_VALUE / 2  // avoid overflow when squaring

    // grid[y][x] = 0 if edge pixel, INF otherwise
    val grid = Array(h) { y ->
        IntArray(w) { x ->
            if (Vec2i.create(x, y) in edgePixels) 0 else INF
        }
    }

    // Row pass
    val rowPassed = Array(h) { y -> edt1d(grid[y]) }

    // Column pass: run edt1d on each column of rowPassed
    val squaredEdt = Array(h) { IntArray(w) }
    val column = IntArray(h)
    for (x in 0 until w) {
        for (y in 0 until h) column[y] = rowPassed[y][x]
        val transformed = edt1d(column)
        for (y in 0 until h) squaredEdt[y][x] = transformed[y]
    }

    val distanceField = BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY)

    val BIAS = 32768
    val SCALE = 1  // 1 pixel of distance = 1 stored unit

    repeat(h) { y ->
        repeat(w) { x ->
            val dist = sqrt(squaredEdt[y][x].toDouble())
            val inside = image.raster.isWhite(x, y)
            val signed = if (inside) dist else -dist

            val encoded = (signed * SCALE + BIAS).coerceIn(0.0, 65535.0).toInt()
            distanceField.raster.setSample(x, y, 0, encoded)
        }
    }

    ImageIO.write(distanceField, "png", File("C:\\Users\\User\\IdeaProjects\\MeshKtRenderer\\proguard\\out.png"))
    return distanceField
}