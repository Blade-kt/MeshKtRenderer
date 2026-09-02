package me.blade.meshkt.renderer.util.vec

interface Vec2 {
    val x: Double
    val y: Double

    private class Impl(
        override val x: Double,
        override val y: Double,
    ) : Vec2

    companion object {
        fun create(x: Double, y: Double): Vec2 = Impl(x, y)
        fun create(x: Float, y: Float): Vec2 = Impl(x.toDouble(), y.toDouble())
        fun create(x: Int, y: Int): Vec2 = Impl(x.toDouble(), y.toDouble())

        val ZERO = create(0, 0)
    }
}