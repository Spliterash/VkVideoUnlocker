package ru.spliterash.vkVideoUnlocker.messageChain.download

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Message
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DefaultVideoChain(
    @GroupUser private val client: VkApi
) : MessageHandler {
    override suspend fun handle(message: Message): Boolean {
        message.reply(client, "Стандартное поведение")

        return true
    }

    override val priority: Int
        get() = Int.MAX_VALUE
}