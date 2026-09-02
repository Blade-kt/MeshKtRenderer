package me.blade.meshkt.renderer.util.vec

interface Vec4 {
    val x: Double
    val y: Double
    val z: Double
    val w: Double

    private class Impl(
        override val x: Double,
        override val y: Double,
        override val z: Double,
        override val w: Double,
    ) : Vec4

    companion object {
        fun create(x: Double, y: Double, z: Double, w: Double): Vec4 = Impl(x, y, z, w)
        fun create(x: Float, y: Float, z: Float, w: Float): Vec4 = Impl(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())
        fun create(x: Int, y: Int, z: Int, w: Int): Vec4 = Impl(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

        val ZERO = create(0, 0, 0, 0)
    }
}