package me.blade.meshkt.renderer.resource

@DslMarker
/**
 * DSL Marker to signal that OpenGL operations in this context are not safe or ready
 * to be executed, so they gonna be deferred to the main thread and to the time, when the resource is ready to be used
 */
annotation class MeshDef3rredContext

@DslMarker
/**
 * DSL Marker to signal that some resource has reached its needed state by deferring to main thead,
 * and could be safely used in the context marked with this annotation
 */
annotation class MeshSyncContext
