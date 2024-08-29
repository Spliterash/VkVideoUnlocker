package ru.spliterash.vkVideoUnlocker.video.accessor

import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import java.net.URL

interface AdvancedVideoAccessor : VideoAccessor {
    val maxQuality: Int
    val maxQualityUrl: URL
    fun preview(): URL

    suspend fun load(quality: Int): InputStreamSource.Info
    suspend fun load(quality: Int, range: String): InputStreamSource.Info
}