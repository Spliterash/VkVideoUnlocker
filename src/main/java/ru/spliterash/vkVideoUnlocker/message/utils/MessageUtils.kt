package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils.Checker
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    @WorkUser private val user: VkApi,
    private val videoService: VideoService,
) {
    private val vkUrlPattern = Pattern.compile("https://vk\\.com/(?<type>video|wall)(?<owner>-?\\d+)_(?<id>\\d+)")
    suspend fun scanForVideoContent(root: RootMessage): VideoHolder? {
        val attachmentContent = scanForAttachment(root, listOf(Checker { it.video }, Checker { it.story }))
        if (attachmentContent != null) {
            return when (attachmentContent) {
                is VkVideo -> videoService.wrap(attachmentContent, root)
                is VkStory -> videoService.wrap(attachmentContent, root)
                else -> throw IllegalArgumentException("Impossible exception")
            }
        }
        val text = root.text?.trim()
        if (text.isNullOrEmpty()) return null

        val matcher = vkUrlPattern.matcher(text)
        if (!matcher.find()) return null

        val id = matcher.group("owner") + "_" + matcher.group("id")
        return when (matcher.group("type")) {
            "video" -> videoService.wrap(id, root)
            "wall" -> {
                val wall = user.walls.getById(id)
                val video = scanForAttachment(wall) { it.video }
                if (video != null)
                    videoService.wrap(video, root)
                else
                    null
            }

            else -> null
        }
    }

    fun <T : AttachmentContent> scanForAttachment(root: AttachmentContainer, checker: Checker<T>): T? {
        return scanForAttachment(root, listOf(checker)) as T?
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun scanForAttachment(root: AttachmentContainer, checkers: List<Checker<*>>): AttachmentContent? {
        for (attachment in root.attachments()) {
            for (checker in checkers) {
                val needle = checker.check(attachment)
                if (needle != null)
                    return needle
            }

            if (attachment.wall != null) {
                val scanResult = scanForAttachment(attachment.wall, checkers)
                if (scanResult != null)
                    return scanResult
            }

            if (attachment.wallReply != null) {
                val scanResult = scanForAttachment(attachment.wallReply, checkers)
                if (scanResult != null)
                    return scanResult
            }
        }
        for (somethingWithAttachments in root.containers()) {
            val result = scanForAttachment(somethingWithAttachments, checkers)
            if (result != null)
                return result
        }

        return null
    }


    fun interface Checker<T : AttachmentContent> {
        fun check(obj: Attachment): T?
    }
}