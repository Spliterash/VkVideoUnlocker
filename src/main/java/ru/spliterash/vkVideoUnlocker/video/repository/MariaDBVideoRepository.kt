package ru.spliterash.vkVideoUnlocker.video.repository

import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity

@Singleton
class MariaDBVideoRepository(
    private val jdbi: Jdbi
) : VideoRepository {
    override suspend fun findVideo(id: String): VideoEntity? = withContext(Dispatchers.IO) {
        jdbi.withHandleUnchecked { handle ->
            handle.createQuery("SELECT * FROM videos where id = ?")
                .bind(0, id)
                .mapTo(VideoEntity::class.java)
                .findOne()
                .orElse(null)
        }
    }

    override suspend fun save(entity: VideoEntity) {
        jdbi.withHandleUnchecked { handle ->
            handle.createUpdate("INSERT INTO videos (id,unlocked_id,private) values (:id,:unlocked_id,:private) " +
                    "on duplicate key update unlocked_id = :unlocked_id, private = :private")
                .bind("id", entity.id)
                .bind("unlocked_id", entity.unlockedId)
                .bind("private", entity.private)
                .execute()
        }
    }
}