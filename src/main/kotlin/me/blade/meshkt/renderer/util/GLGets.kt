package me.blade.meshkt.renderer.util

import me.blade.meshkt.renderer.util.vec.Vec2
import me.blade.meshkt.renderer.util.vec.Vec2i
import me.blade.meshkt.renderer.util.vec.Vec3
import me.blade.meshkt.renderer.util.vec.Vec3i
import me.blade.meshkt.renderer.util.vec.Vec4
import me.blade.meshkt.renderer.util.vec.Vec4i
import org.lwjgl.opengl.GL11C.glGetDoublev
import org.lwjgl.opengl.GL11C.glGetFloatv
import org.lwjgl.opengl.GL11C.glGetIntegerv

private val fbuffer = FloatArray(4)
private val dbuffer = DoubleArray(4)
private val ibuffer = IntArray(4)

fun glGetFloat2(pname: Int) = fbuffer
    .also { glGetFloatv(pname, it) }
    .let { Vec2.create(it[0], it[1]) }

fun glGetDouble2(pname: Int) = dbuffer
    .also { glGetDoublev(pname, it) }
    .let { Vec2.create(it[0], it[1]) }

fun glGetInt2(pname: Int) = ibuffer
    .also { glGetIntegerv(pname, it) }
    .let { Vec2i.create(it[0], it[1]) }

fun glGetFloat3(pname: Int) = fbuffer
    .also { glGetFloatv(pname, it) }
    .let { Vec3.create(it[0], it[1], it[2]) }

fun glGetDouble3(pname: Int) = dbuffer
    .also { glGetDoublev(pname, it) }
    .let { Vec3.create(it[0], it[1], it[2]) }

fun glGetInt3(pname: Int) = ibuffer
    .also { glGetIntegerv(pname, it) }
    .let { Vec3i.create(it[0], it[1], it[2]) }

fun glGetFloat4(pname: Int) = fbuffer
    .also { glGetFloatv(pname, it) }
    .let { Vec4.create(it[0], it[1], it[2], it[3]) }

fun glGetDouble4(pname: Int) = dbuffer
    .also { glGetDoublev(pname, it) }
    .let { Vec4.create(it[0], it[1], it[2], it[3]) }

fun glGetInt4(pname: Int) = ibuffer
    .also { glGetIntegerv(pname, it) }
    .let { Vec4i.create(it[0], it[1], it[2], it[3]) }

fun glGetFloatRange(pname: Int) = fbuffer
    .also { glGetFloatv(pname, it) }
    .let { it[0]..it[1] }

fun glGetDoubleRange(pname: Int) = dbuffer
    .also { glGetDoublev(pname, it) }
    .let { it[0]..it[1] }

fun glGetIntRange(pname: Int) = ibuffer
    .also { glGetIntegerv(pname, it) }
    .let { it[0]..it[1] }