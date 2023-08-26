package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.vk.AttachmentScanner
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    private val attachmentScanner: AttachmentScanner,
    private val videoService: VideoService,
) {
    private val vkUrlPattern = Pattern.compile("(?:https?://)?vk\\.com/(?<attachment>(?:video|wall|story)-?\\d+_\\d+)")
    suspend fun scanForVideoContent(root: RootMessage): VideoContentHolder? {
        val attachmentContent = attachmentScanner.scanForAttachment(
            root,
            listOf(
                AttachmentScanner.Checker { it.video },
                AttachmentScanner.Checker { it.story }
            )
        )
        if (attachmentContent != null) {
            return when (attachmentContent) {
                is VkVideo -> videoService.wrap(attachmentContent)
                is VkStory -> videoService.wrap(attachmentContent)
                else -> throw IllegalArgumentException("Impossible exception")
            }
        }
        val text = root.text?.trim()
        if (text.isNullOrEmpty()) return null

        val matcher = vkUrlPattern.matcher(text)
        if (!matcher.find()) return null

        val attachmentId = matcher.group("attachment")

        return videoService.wrapAttachmentId(attachmentId)
    }
}