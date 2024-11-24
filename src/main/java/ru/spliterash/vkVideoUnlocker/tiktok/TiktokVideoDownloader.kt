package ru.spliterash.vkVideoUnlocker.tiktok

import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor


interface TiktokVideoDownloader {
    suspend fun download(videoUrl: String): VideoAccessor
}