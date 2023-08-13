package ru.spliterash.vkVideoUnlocker.application

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class VkUnlockerRunner(
    @GroupUser private val api: VkApi
) : ApplicationEventListener<StartupEvent> {
    override fun onApplicationEvent(event: StartupEvent) = runBlocking {
        api.groups.getLongPollServer()
        Unit
    }
}