package me.blade.meshkt.layout

import me.blade.meshkt.layout.appearance.Appearance
import me.blade.meshkt.layout.display.Padding
import me.blade.meshkt.layout.display.PositionContent
import me.blade.meshkt.layout.display.PositionSelf
import me.blade.meshkt.layout.display.Size
import me.blade.meshkt.layout.hierarchy.ChildRef
import me.blade.meshkt.layout.hierarchy.LayoutState
import me.blade.meshkt.layout.hierarchy.UpdateConvention
import kotlin.jvm.java

open class Layout {
    var key: String? = null

    /**
     * Describes to the Layout system how to position this element
     *
     * @see PositionSelf
     */
    var positionSelf: PositionSelf = PositionSelf.Inherit

    /**
     * Describes to the Layout system how to position the children of this Layout
     *
     * @see PositionContent
     */
    var positionContent: PositionContent = PositionContent()

    /**
     * The width of this layout.
     */
    var width: Size = Size.ByContent

    /**
     * The height of this layout.
     */
    var height: Size = Size.ByContent

    /**
     * Space between this element's border and its content.
     */
    var padding: Padding = Padding.None

    /**
     * Visual styling descriptor of a UI component.
     * Handles backgrounds, borders, shadows, and text appearance.
     */
    var appearance: Appearance = Appearance()

    var opacity = 100

    val state = LayoutState()
    var owner: Layout? = null
    val root: Layout get() = owner?.root ?: this

    val children = arrayListOf<Layout>()
    val childrenRefArray = arrayListOf<ChildRef>()

    fun include(update: UpdateConvention = UpdateConvention.Static, block: () -> Layout) {
        childrenRefArray.add(
            ChildRef(update) {
                listOf(block())
            }
        )
    }

    fun includeAll(resolver: UpdateConvention = UpdateConvention.Static, block: () -> Collection<Layout>) {
        childrenRefArray.add(ChildRef(resolver, block))
    }

    inline fun <reified T: Layout> find(key: String, block: (T) -> Unit = {}) =
        findRecursive(root, key, T::class.java)?.let { block(it); it }

    fun <T: Layout> findRecursive(layout: Layout, key: String, clazz: Class<T>): T? {
        if (layout.key == key && clazz.isInstance(layout)) {
            @Suppress("UNCHECKED_CAST")
            return layout as T
        }

        for (child in layout.children) {
            val result = findRecursive(child, key, clazz)
            if (result != null) {
                return result
            }
        }

        return null
    }

    // Public API with reified (inline but not recursive)
    inline fun <reified T : Layout> get(key: String): T? {
        return findRecursive(root, key, T::class.java)
    }

    companion object {
        /**
         * The fundamental building block for UI composition.
         *
         * A Layout represents a rectangular region in the UI that can contain child elements,
         * handle events, and define positioning rules. Layouts can be nested to create
         * complex interfaces.
         */
        fun layout(block: Layout.() -> Unit) =
            Layout().apply(block)
    }
}