package ru.spliterash.vkVideoUnlocker.video.accessor

import java.io.InputStream
import java.net.URL

interface VideoAccessor {
    suspend fun size(quality: Int): Long
    suspend fun load(): Info

    data class Info(
        val code: Int,
        val stream: InputStream,
        val contentRange: String?,
        val contentLength: Long
    )
}