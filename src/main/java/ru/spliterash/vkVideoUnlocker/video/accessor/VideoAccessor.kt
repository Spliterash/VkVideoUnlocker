package ru.spliterash.vkVideoUnlocker.video.accessor

import java.io.InputStream

interface VideoAccessor {
    suspend fun size(): Long
    suspend fun load(): Info
    suspend fun load(range: String): Info

    data class Info(
        val code: Int,
        val stream: InputStream,
        val contentRange: String?,
        val contentLength: Long
    )
}