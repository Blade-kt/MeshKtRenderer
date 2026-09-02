package me.blade.meshkt.renderer.util.vec

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