package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    private val videoService: VideoService,
) {
    suspend fun scanForVideo(root: RootMessage): VkVideo? {
        val simpleWayVideo = scanForAttachment(root) { it.video }
        if (simpleWayVideo != null)
            return simpleWayVideo

        val text = root.text ?: return null

        return null
    }

    fun <T> scanForAttachment(root: AttachmentContainer, checker: Checker<T>): T? {
        for (attachment in root.attachments()) {
            val needle = checker.check(attachment)
            if (needle != null)
                return needle

            if (attachment.wall != null) {
                val scanResult = scanForAttachment(attachment.wall, checker)
                if (scanResult != null)
                    return scanResult
            }

            if (attachment.wallReply != null) {
                val scanResult = scanForAttachment(attachment.wallReply, checker)
                if (scanResult != null)
                    return scanResult
            }
        }
        for (somethingWithAttachments in root.containers()) {
            val result = scanForAttachment(somethingWithAttachments, checker)
            if (result != null)
                return result
        }

        return null
    }


    fun interface Checker<T> {
        fun check(obj: Attachment): T?
    }
}