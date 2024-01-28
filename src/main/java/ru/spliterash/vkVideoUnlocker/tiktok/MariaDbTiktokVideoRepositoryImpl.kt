package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked

@Singleton
class MariaDbTiktokVideoRepositoryImpl(
    private val jdbi: Jdbi
) : TiktokVideoRepository {
    override suspend fun findVideo(id: String): TiktokVideoEntity? = jdbi.withHandleUnchecked { handle ->
        handle.createQuery("SELECT * FROM tiktok_videos where id = ?")
            .bind(0, id)
            .mapTo(TiktokVideoEntity::class.java)
            .findOne()
            .orElse(null)
    }

    override suspend fun save(entity: TiktokVideoEntity) {
        jdbi.withHandleUnchecked { handle ->
            handle.createUpdate(
                "INSERT INTO tiktok_videos (id,vk_id) values (:id,:vk_id) " +
                        "on duplicate key update vk_id = :vk_id"
            )
                .bind("id", entity.id)
                .bind("vk_id", entity.vkId)
                .execute()
        }
    }
}