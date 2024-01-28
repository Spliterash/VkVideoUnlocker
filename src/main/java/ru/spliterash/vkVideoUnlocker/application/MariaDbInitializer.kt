package ru.spliterash.vkVideoUnlocker.application

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity

@Singleton
class MariaDbInitializer(
    private val jdbi: Jdbi
) : ApplicationEventListener<StartupEvent> {
    override fun onApplicationEvent(event: StartupEvent)  {
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
}