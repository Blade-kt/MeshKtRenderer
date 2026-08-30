package me.blade.meshkt.renderer.util

import java.awt.Color

/**
 * Packs 2 integer values into a single 32-bit integer with specified bit widths.
 * Values are packed MSB to LSB: [firstValue] [secondValue]
 */
fun packVec2(
    firstSizeBits: Int, secondSizeBits: Int,
    firstValue: Int, secondValue: Int
): Int {
    require(firstSizeBits + secondSizeBits == Int.SIZE_BITS) {
        "Bit sizes of components must sum to Int.SIZE_BITS (32), got ${firstSizeBits + secondSizeBits}"
    }

    val firstMax = (1 shl firstSizeBits) - 1
    val secondMax = (1 shl secondSizeBits) - 1

    require(firstValue in 0..firstMax) {
        "First value $firstValue must be in range 0..$firstMax (${firstSizeBits}-bit)"
    }
    require(secondValue in 0..secondMax) {
        "Second value $secondValue must be in range 0..$secondMax (${secondSizeBits}-bit)"
    }

    return (firstValue shl secondSizeBits) or
            secondValue
}

/**
 * Packs 3 integer values into a single 32-bit integer with specified bit widths.
 * Values are packed MSB to LSB: [firstValue] [secondValue] [thirdValue]
 */
fun packVec3(
    firstSizeBits: Int, secondSizeBits: Int, thirdSizeBits: Int,
    firstValue: Int, secondValue: Int, thirdValue: Int
): Int {
    require(firstSizeBits + secondSizeBits + thirdSizeBits == Int.SIZE_BITS) {
        "Bit sizes of components must sum to Int.SIZE_BITS (32), got ${firstSizeBits + secondSizeBits + thirdSizeBits}"
    }

    val firstMax = (1 shl firstSizeBits) - 1
    val secondMax = (1 shl secondSizeBits) - 1
    val thirdMax = (1 shl thirdSizeBits) - 1

    require(firstValue in 0..firstMax) {
        "First value $firstValue must be in range 0..$firstMax (${firstSizeBits}-bit)"
    }
    require(secondValue in 0..secondMax) {
        "Second value $secondValue must be in range 0..$secondMax (${secondSizeBits}-bit)"
    }
    require(thirdValue in 0..thirdMax) {
        "Third value $thirdValue must be in range 0..$thirdMax (${thirdSizeBits}-bit)"
    }

    return (firstValue shl (thirdSizeBits + secondSizeBits)) or
            (secondValue shl thirdSizeBits) or
            thirdValue
}

/**
 * Packs 4 integer values into a single 32-bit integer with specified bit widths.
 * Values are packed MSB to LSB: [firstValue] [secondValue] [thirdValue] [fourthValue]
 */
fun packVec4(
    firstSizeBits: Int, secondSizeBits: Int, thirdSizeBits: Int, fourthSizeBits: Int,
    firstValue: Int, secondValue: Int, thirdValue: Int, fourthValue: Int
): Int {
    require(firstSizeBits + secondSizeBits + thirdSizeBits + fourthSizeBits == Int.SIZE_BITS) {
        "Bit sizes of components must sum to Int.SIZE_BITS (32), got ${firstSizeBits + secondSizeBits + thirdSizeBits + fourthSizeBits}"
    }

    val firstMax = (1 shl firstSizeBits) - 1
    val secondMax = (1 shl secondSizeBits) - 1
    val thirdMax = (1 shl thirdSizeBits) - 1
    val fourthMax = (1 shl fourthSizeBits) - 1

    require(firstValue in 0..firstMax) {
        "First value $firstValue must be in range 0..$firstMax (${firstSizeBits}-bit)"
    }
    require(secondValue in 0..secondMax) {
        "Second value $secondValue must be in range 0..$secondMax (${secondSizeBits}-bit)"
    }
    require(thirdValue in 0..thirdMax) {
        "Third value $thirdValue must be in range 0..$thirdMax (${thirdSizeBits}-bit)"
    }
    require(fourthValue in 0..fourthMax) {
        "Fourth value $fourthValue must be in range 0..$fourthMax (${fourthSizeBits}-bit)"
    }

    return (firstValue shl (fourthSizeBits + thirdSizeBits + secondSizeBits)) or
            (secondValue shl (fourthSizeBits + thirdSizeBits)) or
            (thirdValue shl fourthSizeBits) or
            fourthValue
}

/**
 * Packs color value into a single 32-bit ARGB integer.
 */
fun packColorARGB(value: Color): Int {
    return packColorARGB(value.alpha, value.red, value.green, value.blue)
}

/**
 * Packs color value into a single 32-bit ARGB integer.
 */
fun packColorARGB(a: Int, r: Int, g: Int, b: Int): Int {
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

/**
 * Packs color value into a single 32-bit ARGB integer.
 */
fun packColorARGB(a: Float, r: Float, g: Float, b: Float): Int {
    val range = 0..255

    val aByte = (a * 255).toInt().coerceIn(range)
    val rByte = (r * 255).toInt().coerceIn(range)
    val gByte = (g * 255).toInt().coerceIn(range)
    val bByte = (b * 255).toInt().coerceIn(range)

    return packColorARGB(aByte, rByte, gByte, bByte)
}

/**
 * Packs color value into a single 32-bit ARGB integer.
 */
fun packColorARGB(a: Double, r: Double, g: Double, b: Double): Int {
    return packColorARGB(a.toFloat(), r.toFloat(), g.toFloat(), b.toFloat())
}