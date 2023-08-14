package ru.spliterash.vkVideoUnlocker.video.api

import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor

interface Videos {
    /**
     * Получить видео
     */
    suspend fun getVideo(id: String): VkVideo

    /**
     * Выгрузить видос
     */
    suspend fun upload(
        groupId: Int,
        name: String,
        private: Boolean,
        accessor: VideoAccessor
    ): String
}