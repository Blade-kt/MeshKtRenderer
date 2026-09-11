package me.blade.meshkt.layout.display

class Padding(
    val paddingTop: Double,
    val paddingRight: Double,
    val paddingBottom: Double,
    val paddingLeft: Double,
) {
    constructor(padding: Double) : this(padding, padding, padding, padding)
    constructor(paddingVertical: Double, paddingHorizontal: Double) : this(paddingVertical, paddingHorizontal, paddingVertical, paddingHorizontal)

    companion object {
        val None = Padding(0.0)
    }
}