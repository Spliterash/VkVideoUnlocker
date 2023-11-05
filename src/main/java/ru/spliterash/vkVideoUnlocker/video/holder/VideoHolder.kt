package ru.spliterash.vkVideoUnlocker.video.holder

interface VideoHolder : VideoContentHolder {
    suspend fun isLocked(): Boolean
}