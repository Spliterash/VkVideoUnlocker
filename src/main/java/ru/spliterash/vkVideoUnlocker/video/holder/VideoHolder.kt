package ru.spliterash.vkVideoUnlocker.video.holder

import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

interface VideoHolder {
    val id: String
    val root: RootMessage

    /**
     * Видео без ссылок на скачивание, чисто инфа
     */
    suspend fun video(): VkVideo
    suspend fun fullVideo(): FullVideo
    val type: VideoHolderType

    enum class VideoHolderType {
        VIDEO, STORY
    }

}