package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.*
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.video.service.VideoReUploadService
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DefaultVideoChain(
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
    private val reUploadService: VideoReUploadService,
) : MessageHandler {
    override suspend fun handle(message: RootMessage): Boolean = coroutineScope {
        val video = try {
            utils.scanForVideoContent(message)
        } catch (ex: VkUnlockerException) {
            handleException(ex, message)
            return@coroutineScope true
        } ?: return@coroutineScope false

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

        val unlockedId: String = try {
            reUploadService.getUnlockedId(video)
        } catch (ex: VkUnlockerException) {
            handleException(ex, message)
            return@coroutineScope true
        } finally {
            notifyJob.cancel()
        }
        val text = if (message.isGroupChat() && message.hasPing(client))
            "Готово. Если мне выдать доступ ко всей переписке, тогда меня не надо будет пинговать, " +
                    "и я смогу разблокировать видосы автоматически, сразу как они прилетают в беседу"
        else null

        message.reply(client, text, "video$unlockedId")

        return@coroutineScope true
    }

    private fun handleException(ex: VkUnlockerException, message: RootMessage) {
        if (message.isPersonalChat())
            throw ex
    }

    override val priority: Int
        get() = Int.MAX_VALUE
}