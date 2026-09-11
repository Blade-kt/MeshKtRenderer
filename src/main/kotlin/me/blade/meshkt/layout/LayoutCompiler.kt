package me.blade.meshkt.layout

import me.blade.meshkt.layout.display.Alignment
import me.blade.meshkt.layout.display.PositionFlow
import me.blade.meshkt.layout.display.PositionSelf
import me.blade.meshkt.layout.display.RelativeTarget
import me.blade.meshkt.layout.display.Size
import me.blade.meshkt.renderer.engine.MeshUI
import me.blade.meshkt.renderer.util.vec.Vec2
import kotlin.math.max

fun updateLayout(layout: Layout, viewportSize: Vec2) {
    evaluateChildren(layout)
    evaluateSize(layout, viewportSize)
    evaluatePosition(layout)
}

private fun evaluateChildren(layout: Layout) {
    val children = layout.children
    val childrenRefArray = layout.childrenRefArray

    children.clear()
    children.addAll(
        childrenRefArray.flatMap {
            it.update()
            it.childrenGroup
        }
    )

    children.forEach {
        it.owner = layout
    }

    children.forEach { child ->
        evaluateChildren(child)
    }
}

private fun evaluateSize(layout: Layout, viewportSize: Vec2) {
    layout.state.width = viewportSize.x
    layout.state.height = viewportSize.y

    layout.children.forEach { child ->
        evaluateChildSize(child)
    }
}

private fun evaluateChildSize(layout: Layout) {
    layout.children.forEach { child ->
        evaluateChildSize(child)
    }

    val inheritPosChildren = layout.children.filter {
        it.positionSelf == PositionSelf.Inherit
    }

    val positionContent = layout.positionContent
    val gapValue = positionContent.gap
    val gapCount = (inheritPosChildren.count() - 1).coerceAtLeast(0)
    val contentSize = when(positionContent.flow) {
        PositionFlow.Row -> Vec2.create(
            inheritPosChildren.sumOf { it.state.width } + gapValue * gapCount,
            inheritPosChildren.maxOfOrNull { it.state.height } ?: 0.0
        )
        PositionFlow.Column -> Vec2.create(
            inheritPosChildren.maxOfOrNull { it.state.width } ?: 0.0,
            inheritPosChildren.sumOf { it.state.height } + gapValue * gapCount,
        )
    }

    layout.state.rawContentWidth = contentSize.x
    layout.state.rawContentHeight = contentSize.y

    layout.state.width = when (val w = layout.width) {
        Size.ByContent -> contentSize.x.coerceAtLeast(MeshUI.fontWidth {
            content = layout.appearance.text
            height = layout.appearance.textHeight
        }) + layout.padding.paddingLeft + layout.padding.paddingRight
        is Size.Fixed -> w.pixels
    }

    layout.state.height = when (val h = layout.height) {
        Size.ByContent -> contentSize.y.coerceAtLeast(layout.appearance.textHeight) + layout.padding.paddingTop + layout.padding.paddingBottom
        is Size.Fixed -> h.pixels
    }
}

private fun evaluatePosition(layout: Layout) {
    layout.state.positionX = 0.0
    layout.state.positionY = 0.0
    evaluateChildPosition(layout)
}

private fun evaluateChildPosition(layout: Layout) {
    val root = layout.root
    val (inheritPosChildren, anchoredPosChildren) = layout.children.partition {
        it.positionSelf == PositionSelf.Inherit
    }

    // Read the size of the content
    val positionContent = layout.positionContent

    // Position children
    val minContentSpace = Vec2.create(
        layout.state.positionX + layout.padding.paddingLeft,
        layout.state.positionY + layout.padding.paddingTop
    )

    val maxContentSpace = Vec2.create(
        layout.state.positionX + layout.state.width - layout.padding.paddingRight,
        layout.state.positionY + layout.state.height - layout.padding.paddingBottom
    )

    val contentSpaceSize = Vec2.create(maxContentSpace.x  - minContentSpace.x, maxContentSpace.y - minContentSpace.y)
    val gapValue = positionContent.gap
    val gapCount = (inheritPosChildren.count() - 1).coerceAtLeast(0)
    val adjustedGap = when (positionContent.alignment) {
        Alignment.SpaceBetween -> {
            val remainingSpace = when (positionContent.flow) {
                PositionFlow.Row -> contentSpaceSize.x - layout.state.rawContentWidth
                PositionFlow.Column -> contentSpaceSize.y - layout.state.rawContentHeight
            }

            max(gapValue, if (gapCount == 0) 0.0 else (remainingSpace / gapCount))
        }
        else -> gapValue
    }

    // Position inherit children
    var baselineStart: Double
    val baselineAlign: Double
    when (positionContent.flow) {
        PositionFlow.Row -> {
            baselineStart = when (positionContent.alignment) {
                Alignment.Start -> minContentSpace.x
                Alignment.SpaceBetween -> minContentSpace.x
                Alignment.Center -> minContentSpace.x + contentSpaceSize.x * 0.5 - layout.state.rawContentWidth * 0.5
                Alignment.End -> maxContentSpace.x - layout.state.rawContentWidth
            }
            baselineAlign = minContentSpace.y + contentSpaceSize.y * 0.5
        }
        PositionFlow.Column -> {
            baselineAlign = minContentSpace.x + contentSpaceSize.x * 0.5
            baselineStart = when (positionContent.alignment) {
                Alignment.Start -> minContentSpace.y
                Alignment.SpaceBetween -> minContentSpace.y
                Alignment.Center -> minContentSpace.y + contentSpaceSize.y * 0.5 - layout.state.rawContentHeight * 0.5
                Alignment.End -> maxContentSpace.y - layout.state.rawContentHeight
            }
        }
    }
    inheritPosChildren.forEach { child ->
        when (positionContent.flow) {
            PositionFlow.Row -> {
                child.state.positionX = baselineStart
                child.state.positionY = baselineAlign - child.state.height * 0.5
                baselineStart += child.state.width + adjustedGap
            }
            PositionFlow.Column -> {
                child.state.positionX = baselineAlign - child.state.width * 0.5
                child.state.positionY = baselineStart
                baselineStart += child.state.height + adjustedGap
            }
        }
    }

    // Position anchored children
    anchoredPosChildren.forEach { child ->
        val anchorData = child.positionSelf as PositionSelf.Anchored

        val anchorTarget = when (anchorData.target) {
            RelativeTarget.Parent -> child.owner ?: throw IllegalStateException("Child owner is null")
            RelativeTarget.Root -> root
        }

        val anchorPosition = Vec2.create(
            anchorTarget.state.positionX + anchorTarget.state.width * anchorData.targetCorner.xContribution,
            anchorTarget.state.positionY + anchorTarget.state.height * anchorData.targetCorner.yContribution
        )
        child.state.positionX = anchorPosition.x - child.state.width * anchorData.thisCorner.xContribution + anchorData.offset.x
        child.state.positionY = anchorPosition.y - child.state.height * anchorData.thisCorner.yContribution + anchorData.offset.y
    }

    layout.children.forEach { child ->
        evaluateChildPosition(child)
    }
}