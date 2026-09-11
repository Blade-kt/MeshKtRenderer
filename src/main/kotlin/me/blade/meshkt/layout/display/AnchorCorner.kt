package me.blade.meshkt.layout.display

enum class AnchorCorner(
    val xContribution: Double,
    val yContribution: Double,
) {
    LeftTop(0.0, 0.0),
    RightTop(1.0, 0.0),
    LeftBottom(0.0, 1.0),
    RightBottom(1.0, 1.0)
}