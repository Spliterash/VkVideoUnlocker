package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.ReplyMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.hasPing
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.vk.MessageScanner
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.*
import java.util.function.Predicate

@Singleton
class MessageContentScanner(
    @GroupUser private val groupUser: VkApi,
    private val messageScanner: MessageScanner,
) {
    fun findContent(root: RootMessage): MessageScanner.ScanResult? {
        val containerPredicate: Predicate<AttachmentContainer> =
            if (root.isPersonalChat() || root.hasPing(groupUser))
                Predicate { true }
            else
                Predicate { it !is ReplyMessage }

        val init = LinkedList<AttachmentContainer>()
        init += root
        return messageScanner.scanForAttachment(
            init,
            listOf(
                MessageScanner.Checker { it.video },
                MessageScanner.Checker { it.story },
            ),
            containerPredicate
        )
    }
}