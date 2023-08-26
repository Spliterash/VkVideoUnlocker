package ru.spliterash.vkVideoUnlocker.video.holder

import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

sealed interface VideoContentHolder {
    /**
     * ID attachment'а
     */
    val attachmentId: String
    /**
     * Видео без ссылок на скачивание, чисто инфа
     */
    suspend fun video(): VkVideo
    suspend fun fullVideo(): FullVideo
}