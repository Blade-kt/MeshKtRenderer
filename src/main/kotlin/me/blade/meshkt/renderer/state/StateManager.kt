package me.blade.meshkt.renderer.state

import java.awt.Color

class StateManager {

    var depthTest = false
    var depthFunc = Func.Less
    var depthMask = true
    var depthRange = DepthRange(0.0, 1.0)
    var depthClamp = false

    var blendEnabled = false
    var blendFunc = BlendFunc(BlendFactor.SrcAlpha, BlendFactor.OneMinusSrcAlpha, BlendFactor.One, BlendFactor.Zero)
    var blendEquation = BlendEquation(BlendEquationMode.FuncAdd)
    var blendColor = Color(0f, 0f, 0f, 0f)

    var colorMask = ColorMask(r = true, g = true, b = true, a = true)

    var cullFace = false
    var cullFaceMode = CullFaceMode.Back
    var frontFace = FrontFace.CounterClockwise
}