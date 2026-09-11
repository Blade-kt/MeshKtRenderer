package me.blade.meshkt

import kotlinx.coroutines.flow.flow
import me.blade.meshkt.layout.Layout
import me.blade.meshkt.layout.updateLayout
import me.blade.meshkt.layout.Layout.Companion.layout
import me.blade.meshkt.layout.appearance.Appearance
import me.blade.meshkt.layout.appearance.ColorSetup
import me.blade.meshkt.layout.display.Alignment
import me.blade.meshkt.layout.display.AnchorCorner
import me.blade.meshkt.layout.display.Padding
import me.blade.meshkt.layout.display.PositionContent
import me.blade.meshkt.layout.display.PositionFlow
import me.blade.meshkt.layout.display.PositionSelf
import me.blade.meshkt.layout.display.RelativeTarget
import me.blade.meshkt.layout.display.Size
import me.blade.meshkt.layout.hierarchy.UpdateConvention
import me.blade.meshkt.renderer.engine.MeshUI
import me.blade.meshkt.renderer.util.vec.Vec2
import org.lwjgl.glfw.GLFW.glfwGetTime
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.math.sin

object LayoutTest {
    val layout = layout {
        include {
            layout {
                positionSelf = PositionSelf.Anchored(
                    offset = Vec2.create(10, 10)
                )

                appearance = Appearance(
                    backgroundColor = ColorSetup.Mono(Color(255, 100, 0, 255)),
                    roundRadius = 10.0
                )

                padding = Padding(10.0, 20.0)

                height = Size.Fixed(300.0)
                positionContent = PositionContent(
                    flow = PositionFlow.Column,
                    alignment = Alignment.End,
                    gap = 10.0
                )

                includeAll(UpdateConvention.Dynamic) {
                    listOf(
                        layout {
                            appearance = Appearance(
                                text = "Hello layout 1",
                                textHeight = 40.0 + (sin(glfwGetTime()) * 10)
                            )
                        },
                        layout {
                            appearance = Appearance(
                                text = "Hello layout ${(glfwGetTime() * 100.0).roundToInt()}",
                                textHeight = 64.0
                            )
                        },
                        layout {
                            positionSelf = PositionSelf.Anchored(
                                target = RelativeTarget.Parent,
                                thisCorner = AnchorCorner.LeftBottom,
                                targetCorner = AnchorCorner.RightBottom,
                                offset = Vec2.create(5, 0)
                            )

                            appearance = Appearance(
                                backgroundColor = ColorSetup.Mono(Color(0, 0, 0, 100)),
                                text = "Right Bottom of parent (with different corner attachment)",
                                textHeight = 30.0,
                                roundRadius = 4.0
                            )

                            padding = Padding(4.0, 8.0)
                        },
                        layout {
                            positionSelf = PositionSelf.Anchored(
                                target = RelativeTarget.Root,
                                thisCorner = AnchorCorner.RightBottom,
                                targetCorner = AnchorCorner.RightBottom,
                                offset = Vec2.create(-50, -50)
                            )

                            appearance = Appearance(
                                backgroundColor = ColorSetup.Mono(Color(0, 0, 0, 100)),
                                text = "Right Bottom of root",
                                textHeight = 30.0,
                                roundRadius = 4.0
                            )

                            padding = Padding(4.0, 8.0)
                        }
                    )
                }
            }
        }
    }

    fun frame() {
        updateLayout(layout, Vec2.create(MeshExample.viewportWidth, MeshExample.viewportHeight))
        render(layout)
    }

    private fun render(layout: Layout) {
        val background = layout.appearance.backgroundColor
        if (background != ColorSetup.None) {
            MeshUI.rect {
                pos1 = Vec2.create(layout.state.positionX, layout.state.positionY)
                pos2 = Vec2.create(layout.state.positionX + layout.state.width, layout.state.positionY + layout.state.height)
                when (background) {
                    is ColorSetup.FullGradient -> {
                        colorLeftTop = background.colorLeftTop
                        colorRightTop = background.colorRightTop
                        colorLeftBottom = background.colorLeftBottom
                        colorRightBottom = background.colorRightBottom
                    }
                    is ColorSetup.HorizontalGradient -> {
                        colorH(background.colorLeft, background.colorRight)
                    }
                    is ColorSetup.VerticalGradient -> {
                        colorV(background.colorTop, background.colorBottom)
                    }
                    is ColorSetup.Mono -> {
                        color(background.color)
                    }
                }
                radius(layout.appearance.roundRadius)
            }
        }

        if (layout.appearance.text.isNotBlank()) {
            MeshUI.text {
                color = Color.WHITE
                content = layout.appearance.text
                height = layout.appearance.textHeight
                pos = Vec2.create(layout.padding.paddingLeft + layout.state.positionX, layout.state.positionY + layout.state.height * 0.5 - layout.appearance.textHeight * 0.5)
            }
        }

        layout.children.forEach {
            render(it)
        }
    }
}