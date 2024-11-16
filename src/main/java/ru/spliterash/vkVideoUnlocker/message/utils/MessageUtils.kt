package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    private val messageContentScanner: MessageContentScanner,
    private val videoService: VideoService,
) {
    private val vkUrlPattern = Pattern.compile("(?:https?://)?vk\\.com/.*?(?<attachment>(?:video|wall|story)-?\\d+_\\d+)")

    suspend fun scanForVideoContent(root: RootMessage): VideoContentHolder? {
        val scanResult = messageContentScanner.findContent(root)
        if (scanResult != null) {
            val (attachmentContent, chain) = scanResult
            return when (attachmentContent) {
                is VkVideo -> videoService.wrap(attachmentContent, chain)
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