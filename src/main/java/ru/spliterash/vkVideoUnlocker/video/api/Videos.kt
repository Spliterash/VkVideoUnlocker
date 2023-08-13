package ru.spliterash.vkVideoUnlocker.video.api

import ru.spliterash.vkVideoUnlocker.video.Video
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoLockedException

interface Videos {
    /**
     * Получить видео
     */
    @Throws(VideoLockedException::class)
    suspend fun getVideo(id: String): Video

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