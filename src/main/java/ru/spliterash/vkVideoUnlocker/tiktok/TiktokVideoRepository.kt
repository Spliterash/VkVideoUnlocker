package ru.spliterash.vkVideoUnlocker.tiktok

interface TiktokVideoRepository {
    suspend fun findVideo(id: String): TiktokVideoEntity?
    suspend fun save(entity: TiktokVideoEntity)
}