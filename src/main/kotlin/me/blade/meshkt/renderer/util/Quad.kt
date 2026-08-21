package me.blade.meshkt.renderer.util

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    companion object {
        fun <T> Quad<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)
    }
}