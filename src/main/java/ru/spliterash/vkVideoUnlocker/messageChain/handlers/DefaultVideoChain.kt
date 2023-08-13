package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DefaultVideoChain(
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
    private val videoService: VideoService,
) : MessageHandler {
    override suspend fun handle(message: RootMessage): Boolean {
        val video = utils.scanForAttachment(message) { it.video } ?: return false
        try {
            videoService.baseCheckVideo(video)
        } catch (ex: VkUnlockerException) {
            if (message.isPersonalChat())
                throw ex
            else
                return true
        }

        val locked = videoService.isLocked(video.normalId())

        if (!locked) {
            if (message.isPersonalChat())
                message.reply(client, "Видео открыто")

            return true
        }
        val unlockedId = videoService.getUnlockedId(video)
        message.reply(client, attachments = "video$unlockedId")

        return false
    }

    override val priority: Int
        get() = Int.MAX_VALUE
}