package me.blade.meshkt.renderer.util.vec

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