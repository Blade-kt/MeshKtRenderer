package me.blade.meshkt.layout.appearance

import java.awt.Color

sealed class ColorSetup {
    object None : ColorSetup()

    class Mono(
        val color: Color
    ) : ColorSetup()

    class HorizontalGradient(
        val colorLeft: Color,
        val colorRight: Color,
    ) : ColorSetup()

    class VerticalGradient(
        val colorTop: Color,
        val colorBottom: Color,
    ) : ColorSetup()

    class FullGradient(
        val colorLeftBottom: Color,
        val colorLeftTop: Color,
        val colorRightTop: Color,
        val colorRightBottom: Color,
    ) : ColorSetup()

    companion object {
        val DEFAULT = Mono(Color.WHITE)
    }
}