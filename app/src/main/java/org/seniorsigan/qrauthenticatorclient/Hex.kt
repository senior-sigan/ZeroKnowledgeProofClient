package org.seniorsigan.qrauthenticatorclient

private val hexArray = "0123456789ABCDEF".toCharArray()

internal fun Byte.toHexString() : String {
    val i = this.toInt()
    val first = hexArray[i shr 4 and 0x0f]
    val second = hexArray[i and 0x0f]
    return "$first$second"
}

internal fun ByteArray.toHexString() : String {
    val builder = StringBuilder()
    for (b in this) {
        builder.append(b.toHexString())
    }
    return builder.toString()
}

internal fun String.fromHex(): ByteArray {
    val data = ByteArray(this.length / 2)
    for (i in 0..this.length - 1 step 2) {
        val block = (Character.digit(this[i], 16) shl 4) + Character.digit(this[i+1], 16)
        data[i/2] = block.toByte()
    }

    return data
}