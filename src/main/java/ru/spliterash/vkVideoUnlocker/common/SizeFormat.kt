package ru.spliterash.vkVideoUnlocker.common

import java.text.DecimalFormat

object SizeFormat {
    private val BYTE = 1L
    private val KiB = BYTE shl 10
    private val MiB = KiB shl 10
    private val GiB = MiB shl 10
    private val TiB = GiB shl 10
    private val PiB = TiB shl 10
    private val EiB = PiB shl 10
    private val DEC_FORMAT = DecimalFormat("#.##")

    private fun formatSize(size: Long, divider: Long, unitName: String): String {
        return DEC_FORMAT.format(size.toDouble() / divider) + " " + unitName
    }


    fun Long.toHumanSizeReadable(): String {
        require(this >= 0) { "Invalid file size: $this" }
        if (this >= EiB) return formatSize(this, EiB, "EiB")
        if (this >= PiB) return formatSize(this, PiB, "PiB")
        if (this >= TiB) return formatSize(this, TiB, "TiB")
        if (this >= GiB) return formatSize(this, GiB, "GiB")
        if (this >= MiB) return formatSize(this, MiB, "MiB")
        return if (this >= KiB) formatSize(this, KiB, "KiB") else formatSize(this, BYTE, "Bytes")
    }
}