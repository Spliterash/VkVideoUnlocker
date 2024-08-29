package ru.spliterash.vkVideoUnlocker.common

import java.io.Closeable
import java.io.InputStream

fun interface InputStreamSource {
    suspend fun load(): Info

    data class Info(
        val code: Int,
        val stream: InputStream,
        val contentRange: String?,
        val contentLength: Long
    ) : Closeable {
        override fun close() = stream.close()
    }
}