plugins {
    kotlin("jvm") version "2.3.21"
}

group = "me.blade"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    val lwjglVersion = project.property("lwjglVersion")
    val kotlinCoroutinesVersion = project.property("kotlinCoroutinesVersion")

    val lwjglNatives = "natives-windows"

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl::$lwjglNatives")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
}

kotlin {
    jvmToolchain(25)
}