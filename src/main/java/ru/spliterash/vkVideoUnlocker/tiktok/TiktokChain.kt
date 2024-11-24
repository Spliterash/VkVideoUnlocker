package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import kotlinx.coroutines.coroutineScope
import ru.spliterash.vkVideoUnlocker.common.MessageNotificationProgressMeter
import ru.spliterash.vkVideoUnlocker.common.RedirectHelper
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler
import ru.spliterash.vkVideoUnlocker.vk.MessageScanner
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.regex.Pattern
import kotlin.math.min


@Singleton
class TiktokChain(
    private val redirectHelper: RedirectHelper,
    private val tiktokService: TiktokService,
    private val messageScanner: MessageScanner,
    @GroupUser private val vkApi: VkApi
) : MessageHandler {
    private val pattern =
        Pattern.compile("https?://([a-zA-Z0-9-]+\\.)*tiktok\\.com(/[^\\s]*)?")
    private val directUrlPattern =
        Pattern.compile("https?://www\\.tiktok\\.com/@[\\w.]*/(?<type>video|photo)/(?<id>\\d+)")

    override suspend fun handle(message: RootMessage, editableMessage: EditableMessage): Boolean = coroutineScope {
        val videoUrlInit = messageScanner.scanForText(message) {
            val matcher = pattern.matcher(it)
            if (!matcher.find()) null
            else matcher.group()
        } ?: return@coroutineScope false
        var videoUrl = videoUrlInit
        var directUrlMatcher = directUrlPattern.matcher(videoUrl)
        if (!directUrlMatcher.find()) {
            videoUrl = redirectHelper.finalUrl(videoUrl)
            directUrlMatcher = directUrlPattern.matcher(videoUrl)

            if (!directUrlMatcher.find()) throw IllegalStateException("Unable to normalize tiktok url. Init $videoUrlInit, after try $videoUrl")
        }
        editableMessage.sendOrUpdate("Начинаем обработку tiktok контента, это может быть долго")
        val contentType = directUrlMatcher.group("type")
        val tiktokVideoId = directUrlMatcher.group("id")
        if (contentType == "video") {
            val id = tiktokService.getVkId(videoUrl, tiktokVideoId, MessageNotificationProgressMeter(editableMessage))
            editableMessage.sendOrUpdate(attachments = "video$id")
        } else if (contentType == "photo") {
            val (musicAttachment, attachmentIds) = tiktokService.getPhotoAttachmentIds(message.peerId, videoUrl)
            val batches = splitListIntoBatches(attachmentIds, 10)
            if (batches.isEmpty()) {
                editableMessage.sendOrUpdate("Не удалось найти содержимое")
            } else {
                editableMessage.sendOrUpdate(attachments = batches[0].joinToString(","))
                if (batches.size > 1) {
                    for (i in 1..<batches.size) {
                        val batch = batches[i]
                        vkApi.messages.sendMessage(
                            peerId = message.peerId,
                            attachments = batch.joinToString(",")
                        )
                    }
                }
                if (musicAttachment != null) {
                    vkApi.messages.sendMessage(
                        peerId = message.peerId,
                        attachments = musicAttachment
                    )
                }
            }

        } else throw IllegalStateException("Unknown tiktok content type: $contentType, full url: $videoUrl")

        return@coroutineScope true
    }

    @Suppress("SameParameterValue")
    private fun <T> splitListIntoBatches(list: List<T>, batchSize: Int): List<List<T>> {
        val batches: MutableList<List<T>> = ArrayList()
        var i = 0
        while (i < list.size) {
            val end = min((i + batchSize).toDouble(), list.size.toDouble()).toInt()
            batches.add(ArrayList(list.subList(i, end)))
            i += batchSize
        }
        return batches
    }
}