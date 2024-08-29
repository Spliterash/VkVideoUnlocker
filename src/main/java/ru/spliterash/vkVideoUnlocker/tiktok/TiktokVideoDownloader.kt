package ru.spliterash.vkVideoUnlocker.tiktok


interface TiktokVideoDownloader {
    suspend fun download(videoUrl: String): TiktokVideo
}