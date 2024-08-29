package ru.spliterash.vkVideoUnlocker.tiktok

interface TiktokPhotoDownloader {
    suspend fun download(url: String): TiktokPhoto
}