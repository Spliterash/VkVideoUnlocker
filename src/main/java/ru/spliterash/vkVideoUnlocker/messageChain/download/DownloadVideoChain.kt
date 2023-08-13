package ru.spliterash.vkVideoUnlocker.messageChain.download

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.video.Routes
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DownloadVideoChain(
    @Value("\${vk-unlocker.domain}") private val domain: String,
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

        if (video == null) {
            if (message.isPersonalChat())
                message.reply(
                    client,
                    "Прикрепи видео к сообщению, ну или перешли его как обычно, чтобы я знал что тебе нужно"
                )
        } else
            message.reply(client, "Скачать: ${domain + Routes.DOWNLOAD.replace("{id}", video.normalId())}")

        return true
    }
}