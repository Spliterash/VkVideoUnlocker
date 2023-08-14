package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DefaultVideoChain(
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
    private val videoService: VideoService,
) : MessageHandler {
    override suspend fun handle(message: RootMessage): Boolean = coroutineScope {
        val video = utils.scanForVideo(message) ?: return@coroutineScope false

        val notifyJob = launch {
            delay(3000)
            message.reply(client, "Видео обрабатывается дольше чем обычно, я не завис")
            delay(10000)
            message.reply(client, "Да да, всё ещё обрабатывается, потерпи чуть чуть")
            delay(20000)
            message.reply(client, "Я не знаю что ты туда положил, но оно всё ещё обрабатывается")
            while (true) {
                delay(30000)
                message.reply(client, "Всё ещё в процессе")
            }
        }

        val unlockedId = try {
            videoService.getUnlockedId(video)
        } catch (ex: VkUnlockerException) {
            if (message.isPersonalChat())
                throw ex

            return@coroutineScope true
        } finally {
            notifyJob.cancel()
        }

        message.reply(client, attachments = "video$unlockedId")

        return@coroutineScope true
    }

    override val priority: Int
        get() = Int.MAX_VALUE
}