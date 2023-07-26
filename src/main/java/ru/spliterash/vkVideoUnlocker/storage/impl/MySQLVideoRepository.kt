package ru.spliterash.vkVideoUnlocker.storage.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.spliterash.utils.database.jdbc.types.mysql.MySQLDatabase
import ru.spliterash.vkVideoUnlocker.storage.VideoEntity
import ru.spliterash.vkVideoUnlocker.storage.VideoRepository

class MySQLVideoRepository(
    private val database: MySQLDatabase
) : VideoRepository {

    init {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("MySQL_init.sql")!!

        val initSQL = stream
            .readAllBytes()
            .decodeToString()
        database.createSession().use { session ->
            for (line in initSQL.split(";")) {
                val trimLine = line.trim()
                if (trimLine.isBlank())
                    continue

                session.update(trimLine)
            }
        }
    }

    override suspend fun findVideo(id: String): VideoEntity? = withContext(Dispatchers.IO) {
        database.query(
            "SELECT * FROM videos where id = ?",
            id
        )
            .first()
            .map {
                VideoEntity(
                    id,
                    it.getObject("unlocked_id").toString()
                )
            }
            .orElse(null)
    }

    override suspend fun save(entity: VideoEntity): Unit = withContext(Dispatchers.IO) {
        database.update(
            "INSERT INTO videos (id,unlocked_id) values (:id,:unlocked_id) on duplicate key update unlocked_id = :unlocked_id",
            mapOf(
                "id" to entity.id,
                "unlocked_id" to entity.unlockedId
            )
        )
    }
}