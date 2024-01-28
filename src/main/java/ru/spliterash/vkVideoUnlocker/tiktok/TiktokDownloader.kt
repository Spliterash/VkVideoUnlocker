package ru.spliterash.vkVideoUnlocker.tiktok


interface TiktokDownloader {
    suspend fun download(videoUrl: String): TiktokVideo
}