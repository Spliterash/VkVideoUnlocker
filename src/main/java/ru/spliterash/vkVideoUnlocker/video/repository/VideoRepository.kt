package ru.spliterash.vkVideoUnlocker.video.repository

import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity

interface VideoRepository {
    suspend fun findVideo(id: String): VideoEntity?
    suspend fun save(entity: VideoEntity)
}