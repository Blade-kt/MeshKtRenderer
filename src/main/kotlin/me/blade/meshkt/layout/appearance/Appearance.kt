package me.blade.meshkt.layout.appearance

import me.blade.meshkt.renderer.util.vec.Vec2

class Appearance(
    /**
     * The fill color for the component's background.
     * Default: [ColorSetup.None] (not rendered).
     */
    val backgroundColor: ColorSetup = ColorSetup.None,

    /**
     * Corner radius applied uniformly to all four corners.
     * Default: 0.px (sharp corners).
     */
    val roundRadius: Double = 0.0,

    /**
     * Color of the border stroke around the component.
     * Default: [ColorSetup.None] (no outline).
     */
    val outlineColor: ColorSetup = ColorSetup.None,

    /**
     * Thickness of the border stroke.
     * Default: 0.px.
     */
    val outlineWidth: Double = 0.0,

    /**
     * Color of the drop shadow effect.
     * Default: [ColorSetup.None] (no shadow).
     */
    val shadowColor: ColorSetup = ColorSetup.None,

    /**
     * How much the shadow expands inward (positive values create inner shadow).
     * Default: 0.px.
     */
    val shadowInnerSpread: Double = 0.0,

    /**
     * How much the shadow expands outward beyond the component bounds.
     * Default: 0.px.
     */
    val shadowOuterSpread: Double = 0.0,

    /**
     * Offset of the shadow position relative to the component.
     * Default: [Vec2.ZERO] (centered).
     */
    val shadowOffset: Vec2 = Vec2.ZERO,

    /**
     * Color of the text content.
     * Default: [ColorSetup.Companion.DEFAULT] (white).
     */
    val textColor: ColorSetup = ColorSetup.DEFAULT,

    /**
     * Height of the text in pixels.
     * Default: 16 pixels.
     */
    val textHeight: Double = 16.0,

    val text: String = "",

    /**
     * Controls text color mapping behavior.
     * - `true`: Gradient acts as a mask over the entire component space.
     * - `false` (default): Gradient is constrained to the text bounds only.
     */
    val rebaseTextGradient: Boolean = false
)