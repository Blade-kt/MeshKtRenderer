package me.blade.meshkt.renderer.state

data class DepthRange(val near: Float, val far: Float) {
    constructor(near: Double, far: Double) : this(near.toFloat(), far.toFloat())
}