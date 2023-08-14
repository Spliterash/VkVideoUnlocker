package ru.spliterash.vkVideoUnlocker.video.repository

import jakarta.annotation.PostConstruct
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity

@Singleton
class MariaDBVideoRepository(
    private val jdbi: Jdbi
) : VideoRepository {
    @PostConstruct
    fun init() {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("MySQL_init.sql")!!

        val initSQL = stream
            .readAllBytes()
            .decodeToString()
        jdbi.registerRowMapper(VideoEntity::class.java, KotlinMapper(VideoEntity::class))

        jdbi.inTransactionUnchecked { handle ->
            handle.begin()

            val batch = handle.createBatch()
            for (line in initSQL.split(";")) {
                val trimLine = line.trim()
                if (trimLine.isBlank())
                    continue

                batch.add(line)
            }

            batch.execute()

            handle.commit()
        }
    }

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
            handle.createUpdate("INSERT INTO videos (id,unlocked_id) values (:id,:unlocked_id) on duplicate key update unlocked_id = :unlocked_id")
                .bind("id", entity.id)
                .bind("unlocked_id", entity.unlockedId)
                .execute()
        }
    }
}