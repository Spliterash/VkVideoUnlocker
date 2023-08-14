package ru.spliterash.vkVideoUnlocker.video.service

import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

interface VideoHolder {
    val id: String

    /**
     * Видео без ссылок на скачивание, чисто инфа
     */
    suspend fun video(): VkVideo
    suspend fun fullVideo(): FullVideo
}