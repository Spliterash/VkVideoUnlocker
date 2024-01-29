package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import ru.spliterash.vkVideoUnlocker.common.MessageNotificationProgressMeter
import ru.spliterash.vkVideoUnlocker.common.RedirectHelper
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.vk.MessageScanner
import java.util.regex.Pattern


@Singleton
class TiktokChain(
    private val redirectHelper: RedirectHelper,
    private val tiktokService: TiktokService,
    private val messageScanner: MessageScanner,
) : MessageHandler {
    private val pattern = Pattern.compile("https?://(?:\\w+)?\\.tiktok\\.com/(?:@[\\w.]+/video/(?:(?<id>\\d+))|\\w+)")
    override suspend fun handle(message: RootMessage, editableMessage: EditableMessage): Boolean = coroutineScope {
        val videoUrl = messageScanner.scanForText(message) {
            val matcher = pattern.matcher(it)
            if (!matcher.find()) null
            else matcher.group()
        } ?: return@coroutineScope false

        val id = tiktokService.getVkId(videoUrl, MessageNotificationProgressMeter(editableMessage))

        editableMessage.sendOrUpdate(attachments = "video$id")
        return@coroutineScope true
    }
}