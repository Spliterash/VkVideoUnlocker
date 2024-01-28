package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.ReplyMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.hasPing
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.vk.MessageScanner
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.function.Predicate
import java.util.regex.Pattern

@Singleton
class MessageUtils(
    @GroupUser private val groupUser: VkApi,
    private val messageScanner: MessageScanner,
    private val videoService: VideoService,
) {
    private val vkUrlPattern = Pattern.compile("(?:https?://)?vk\\.com/(?<attachment>(?:video|wall|story)-?\\d+_\\d+)")
    suspend fun scanForVideoContent(root: RootMessage): VideoContentHolder? {
        val containerPredicate: Predicate<AttachmentContainer> =
            if (root.isPersonalChat() || root.hasPing(groupUser))
                Predicate { true }
            else
                Predicate { it !is ReplyMessage }

        val attachmentContent = messageScanner.scanForAttachment(
            root,
            listOf(
                MessageScanner.Checker { it.video },
                MessageScanner.Checker { it.story }
            ),
            containerPredicate
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