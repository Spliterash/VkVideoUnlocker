package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.video.DownloadUrlSupplier
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DownloadVideoChain(
    private val downloadUrlSupplier: DownloadUrlSupplier,
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
) : ActivationMessageHandler(
    "скачать",
    "загрузить",
    "стянуть",
    "download"
) {

    override suspend fun handleAfterCheck(message: RootMessage) {
        val videoHolder = utils.scanForVideoContent(message)

        if (videoHolder == null) {
            message.reply(
                client,
                "Прикрепи видео к сообщению, ну или перешли его как обычно, чтобы я знал что тебе нужно"
            )
        } else {
            val url = downloadUrlSupplier.downloadUrl(videoHolder.attachmentId)
            message.reply(client, "Скачать: $url")
        }
    }
}