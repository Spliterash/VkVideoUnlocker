package ru.spliterash.vkVideoUnlocker.storage

interface VideoRepository {
    suspend fun findVideo(id: String): VideoEntity?
    suspend fun save(entity: VideoEntity)
}