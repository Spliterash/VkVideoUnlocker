package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.hasPing
import ru.spliterash.vkVideoUnlocker.longpoll.message.isGroupChat
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.video.DownloadUrlSupplier
import ru.spliterash.vkVideoUnlocker.video.exceptions.NoSenseReuploadUserVideos
import ru.spliterash.vkVideoUnlocker.video.exceptions.PrivateVideoDisabledException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoTooLongException
import ru.spliterash.vkVideoUnlocker.video.service.VideoReUploadService
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class DefaultVideoChain(
    @GroupUser private val client: VkApi,
    private val utils: MessageUtils,
    private val reUploadService: VideoReUploadService,
    private val downloadUrlSupplier: DownloadUrlSupplier,
) : MessageHandler {
    override suspend fun handle(message: RootMessage, editableMessage: EditableMessage): Boolean = coroutineScope {
        val video = try {
            utils.scanForVideoContent(message)
        } catch (ex: VkUnlockerException) {
            handleException(ex, message)
            return@coroutineScope true
        } ?: return@coroutineScope false
        if (video.ownerId > 0) {
            handleException(NoSenseReuploadUserVideos(), message)
            return@coroutineScope true
        }


        val notifyJob = launch {
            delay(3000)
            editableMessage.sendOrUpdate("Видео обрабатывается дольше чем обычно, я не завис")
            delay(15000)
            editableMessage.sendOrUpdate("Да да, всё ещё обрабатывается, потерпи чуть чуть")
            delay(20000)
            editableMessage.sendOrUpdate("Я не знаю что ты туда положил, но оно всё ещё обрабатывается")

            var counter = 1;
            while (true) {
                delay(30000)
                editableMessage.sendOrUpdate("Всё ещё в процессе: ${counter++}")
            }
        }
        val unlockedId: String = try {
            reUploadService.getUnlockedId(video).id
        } catch (ex: PrivateVideoDisabledException) {
            val url = downloadUrlSupplier.downloadUrl(video.attachmentId)
            editableMessage.sendOrUpdate("Перезалив видео из закрытых групп временно отключён, но если очень хочется посмотреть, то вот\n$url")
            return@coroutineScope true
        } catch (ex: VideoTooLongException) {
            val url = downloadUrlSupplier.downloadUrl(video.attachmentId)
            editableMessage.sendOrUpdate("Видео длиннее 5 минут, поэтому перезаливать его как отдельное мы не будем. Но вы можете посмотреть его по ссылке:\n$url")
            return@coroutineScope true
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

        editableMessage.sendOrUpdate(text, "video$unlockedId")

        return@coroutineScope true
    }

    private fun handleException(ex: VkUnlockerException, message: RootMessage) {
        if (message.isPersonalChat() || ex is AlwaysNotifyException)
            throw ex
    }

    override val priority: Int
        get() = Int.MAX_VALUE
}