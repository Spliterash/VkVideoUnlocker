package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.video.service.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    @WorkUser private val user: VkApi,
    private val videoService: VideoService,
) {
    private val vkUrlPattern = Pattern.compile("https://vk\\.com/(?<type>video|wall)(?<owner>-?\\d+)_(?<id>\\d+)")
    suspend fun scanForVideo(root: RootMessage): VideoHolder? {
        val infoVideo = scanForAttachment(root) { it.video }
        if (infoVideo != null)
            return videoService.wrap(infoVideo)
        val text = root.text?.trim()
        if (text.isNullOrEmpty()) return null

        val matcher = vkUrlPattern.matcher(text)
        if (!matcher.find()) return null

        val id = matcher.group("owner") + "_" + matcher.group("id")
        return when (matcher.group("type")) {
            "video" -> videoService.wrap(id)
            "wall" -> {
                val wall = user.walls.getById(id)
                val video = scanForAttachment(wall) { it.video }
                if (video != null)
                    videoService.wrap(video)
                else
                    null
            }

            else -> null
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
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