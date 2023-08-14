package ru.spliterash.vkVideoUnlocker.application

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import javax.sql.DataSource

@Singleton
class VkUnlockerRunner(
) : ApplicationEventListener<StartupEvent> {
    override fun onApplicationEvent(event: StartupEvent) = runBlocking {
    }
}