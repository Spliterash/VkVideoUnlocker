package ru.spliterash.vkVideoUnlocker.video.accessor

import ru.spliterash.vkVideoUnlocker.common.InputStreamSource

interface VideoAccessor : InputStreamSource {
    suspend fun size(quality: Int): Long
}