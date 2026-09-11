package me.blade.meshkt.layout.display

/**
 * Defines how a Layout determines its size.
 */
sealed class Size {
    /**
     * Sizes the layout to fit its content. (Default)
     *
     * The layout measures all children and calculates the minimum
     * dimensions needed to contain them.
     */
    object ByContent : Size()

    /**
     * Fixed size in pixels.
     * @property pixels The exact dimension size. Must be positive.
     */
    class Fixed(
        val pixels: Double,
    ) : Size() {
        init {
            require(pixels > 0) {
                "Size must be positive: $pixels"
            }
        }
    }
}