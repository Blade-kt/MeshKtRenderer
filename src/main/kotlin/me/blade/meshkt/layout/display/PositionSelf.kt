package me.blade.meshkt.layout.display

import me.blade.meshkt.renderer.util.vec.Vec2

/**
 * Used to describe to the Layout system how to position this element
 *
 * @see Inherit
 *
 * @see Anchored
 */
sealed class PositionSelf {
    /**
     * Positions this Layout element as no one knows how it works, but it works (Default)
     *
     * P.S. - the positioning is controlled by the parent
     */
    object Inherit : PositionSelf()

    /**
     * Positions this layout element relative to another layout's corner.
     *
     * The element is placed at the specified corner of the target layout,
     * with an optional offset applied.
     *
     * Also, the layout gets removed from the normal document flow so it doesn't use any space.
     */
    class Anchored(
        /**
         * The layout element that serves as the positional reference.
         */
        val target: RelativeTarget = RelativeTarget.Parent,

        /**
         * Which corner of THIS element aligns to the target corner.
         */
        val thisCorner: AnchorCorner = AnchorCorner.LeftTop,

        /**
         * Which corner of the TARGET layout this element aligns to.
         */
        val targetCorner: AnchorCorner = AnchorCorner.LeftTop,

        /**
         * Pixel offset from the target's corner.
         */
        val offset: Vec2 = Vec2.ZERO
    ) : PositionSelf()
}