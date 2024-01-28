package ru.spliterash.vkVideoUnlocker.video.accessor

import java.net.URL

interface AdvancedVideoAccessor : VideoAccessor {
    val maxQuality: Int
    val maxQualityUrl: URL
    fun preview(): URL

    suspend fun load(quality: Int): VideoAccessor.Info
    suspend fun load(quality: Int, range: String): VideoAccessor.Info
}