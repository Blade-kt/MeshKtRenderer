package me.blade.meshkt.renderer.engine.allocators

import me.blade.meshkt.renderer.engine.descriptors.ScissorData
import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.util.vec.Vec2
import kotlin.math.max
import kotlin.math.min

class ScissorStack(val buffer: Buffer) {
    private val cache = hashMapOf<Int, Int>()
    private val data = arrayListOf<ScissorData>()
    private val stack = arrayListOf<ScissorData>()

    var activeScissorSlot = -1; private set

    fun push(data: ScissorData, clamp: Boolean) {
        var dataToPut = data

        if (clamp) run {
            val topLevelData = stack.lastOrNull() ?: return@run

            if (dataToPut.pos1.x > topLevelData.pos1.x &&
                dataToPut.pos1.y > topLevelData.pos1.y &&
                dataToPut.pos2.x < topLevelData.pos2.x &&
                dataToPut.pos2.y < topLevelData.pos2.y) return@run

            dataToPut = ScissorData(
                Vec2.create(
                    max(dataToPut.pos1.x, topLevelData.pos1.x),
                    max(dataToPut.pos1.y, topLevelData.pos1.y)
                ),
                Vec2.create(
                    min(dataToPut.pos2.x, topLevelData.pos2.x),
                    min(dataToPut.pos2.y, topLevelData.pos2.y)
                )
            )
        }

        stack.add(dataToPut)
        updateScissorSlot()
    }

    fun pop() {
        stack.removeLast()
        updateScissorSlot()
    }

    private fun updateScissorSlot() {
        activeScissorSlot = stack.lastOrNull()?.let { scissorData ->
            cache.getOrPut(scissorData.hashCode()) {
                data.add(scissorData)
                data.lastIndex
            }
        } ?: -1
    }

    fun reset() {
        cache.clear()
        data.clear()

        check(stack.isEmpty()) {
            "ScissorStack is not empty at reset phase. Check all stackPush/stackPop calls"
        }

        activeScissorSlot = -1
    }

    fun flush() {
        buffer.reset()
        data.forEach { scissorData ->
            buffer.vec2(scissorData.pos1)
            buffer.vec2(scissorData.pos2)
        }
        buffer.upload()
    }
}