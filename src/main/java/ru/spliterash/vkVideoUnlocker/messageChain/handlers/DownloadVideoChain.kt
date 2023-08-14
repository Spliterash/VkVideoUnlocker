package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.video.Routes
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DownloadVideoChain(
    @Value("\${vk-unlocker.domain}") private val domain: String,
    @GroupUser private val client: VkApi,
    private val videoService: VideoService,
    private val utils: MessageUtils,
) : ActivationMessageHandler(
    "скачать",
    "загрузить",
    "стянуть",
    "download"
) {

    override suspend fun handleAfterCheck(message: RootMessage) {
        val video = utils.scanForVideo(message)

        if (video == null) {
            message.reply(
                client,
                "Прикрепи видео к сообщению, ну или перешли его как обычно, чтобы я знал что тебе нужно"
            )
        } else {
            videoService.baseCheckVideo(video)
            message.reply(client, "Скачать: ${domain + Routes.DOWNLOAD.replace("{id}", video.normalId())}")
        }
    }
}