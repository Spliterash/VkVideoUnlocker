package ru.spliterash.vkVideoUnlocker.video.accessor

import java.io.InputStream
import java.net.URL

interface VideoAccessor {
    val maxQuality: Int
    val maxQualityUrl: URL

    fun preview(): URL
    suspend fun size(quality: Int): Long
    suspend fun load() = load(maxQuality)
    suspend fun load(quality: Int): Info
    suspend fun load(quality: Int, range: String): Info

    data class Info(
        val code: Int,
        val stream: InputStream,
        val contentRange: String?,
        val contentLength: Long
    )
}