package me.blade.meshkt.renderer.util.vec

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