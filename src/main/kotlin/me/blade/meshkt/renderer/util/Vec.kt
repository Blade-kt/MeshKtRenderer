package me.blade.meshkt.renderer.util

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

interface Vec2i {
    val x: Int
    val y: Int

    private class Impl(
        override val x: Int,
        override val y: Int
    ) : Vec2i

    companion object {
        fun create(x: Int, y: Int): Vec2i = Impl(x, y)
        val ZERO = create(0, 0)
    }
}

interface Vec3i {
    val x: Int
    val y: Int
    val z: Int

    private class Impl(
        override val x: Int,
        override val y: Int,
        override val z: Int
    ) : Vec3i

    companion object {
        fun create(x: Int, y: Int, z: Int): Vec3i = Impl(x, y, z)
        val ZERO = create(0, 0, 0)
    }
}

interface Vec4i {
    val x: Int
    val y: Int
    val z: Int
    val w: Int

    private class Impl(
        override val x: Int,
        override val y: Int,
        override val z: Int,
        override var w: Int
    ) : Vec4i

    companion object {
        fun create(x: Int, y: Int, z: Int, w: Int): Vec4i = Impl(x, y, z, w)
        val ZERO = create(0, 0, 0, 0)
    }
}