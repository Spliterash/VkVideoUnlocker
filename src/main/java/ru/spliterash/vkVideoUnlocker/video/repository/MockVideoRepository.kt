package ru.spliterash.vkVideoUnlocker.video.repository

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity

@Singleton
class MockVideoRepository : VideoRepository {
    override suspend fun findVideo(id: String): VideoEntity? {
        return null
    }

    override suspend fun save(entity: VideoEntity) {}
}