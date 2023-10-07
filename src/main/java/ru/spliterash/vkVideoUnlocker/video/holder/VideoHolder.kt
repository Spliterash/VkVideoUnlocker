package ru.spliterash.vkVideoUnlocker.video.holder

interface VideoHolder : VideoContentHolder {
    val videoId: String

    suspend fun isLocked(): Boolean
}