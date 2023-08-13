package ru.spliterash.vkVideoUnlocker.messageChain.download

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DownloadVideoChain(
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
) : MessageHandler {
    private val activationWords = setOf<String>(
        "скачать",
        "загрузить",
        "стянуть",
        "download"
    )

    override suspend fun handle(message: RootMessage): Boolean {
        val text = message.text ?: return true

        if (!activationWords.contains(text.trim().lowercase()))
            return false

        val video = utils.scanForAttachment(message) { it.video }

        if (video == null)
            message.reply(client, "Видос не нашли")
        else
            message.reply(client, "Ты имел ввиду ${video.title}")

        return true
    }
}