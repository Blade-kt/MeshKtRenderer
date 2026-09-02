package me.blade.meshkt.renderer.util.vec

interface Vec3 {
    val x: Double
    val y: Double
    val z: Double

    private class Impl(
        override val x: Double,
        override val y: Double,
        override val z: Double,
    ) : Vec3

    companion object {
        fun create(x: Double, y: Double, z: Double): Vec3 = Impl(x, y, z)
        fun create(x: Float, y: Float, z: Float): Vec3 = Impl(x.toDouble(), y.toDouble(), z.toDouble())
        fun create(x: Int, y: Int, z: Int): Vec3 = Impl(x.toDouble(), y.toDouble(), z.toDouble())

        val ZERO = create(0, 0, 0)
    }
}