package ru.spliterash.vkVideoUnlocker.vk

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.Message
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent
import java.util.*
import java.util.function.Predicate

@Singleton
class MessageScanner {
    fun <T> scanForText(message: Message, func: (String) -> T?): T? {
        val text = message.text
        if (text != null) {
            val result = func(text)
            if (result != null) return result
        }
        for (attachment in message.attachments) {
            val link = attachment.link ?: continue
            val result = func(link.url)
            if (result != null) return result
        }

        for (fwdMessage in message.fwdMessages) {
            val result = scanForText(fwdMessage, func)
            if (result != null) return result
        }

        return null
    }

    fun <T : AttachmentContent> scanForAttachment(
        root: AttachmentContainer,
        checker: Checker<T>,
    ): T? {
        val init = LinkedList<AttachmentContainer>()
        init += root
        return scanForAttachment(init, listOf(checker))?.content as T?
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun scanForAttachment(
        chain: LinkedList<AttachmentContainer>,
        checkers: List<Checker<*>>,
        allowedContainerChecker: Predicate<AttachmentContainer> = Predicate { true }
    ): ScanResult? {
        val root = chain.last()
        if (!allowedContainerChecker.test(root)) return null
        for (attachment in root.attachments()) {
            for (checker in checkers) {
                val needle = checker.check(attachment)
                if (needle != null)
                    return ScanResult(needle, chain)
            }

            if (attachment.wall != null) {
                chain.add(attachment.wall)
                val scanResult = scanForAttachment(chain, checkers, allowedContainerChecker)
                if (scanResult != null)
                    return scanResult
                chain.removeLast()
            }

            if (attachment.wallReply != null) {
                chain.add(attachment.wallReply)
                val scanResult = scanForAttachment(chain, checkers, allowedContainerChecker)
                if (scanResult != null)
                    return scanResult
                chain.removeLast()
            }
        }
        for (somethingWithAttachments in root.containers()) {
            chain.add(somethingWithAttachments)
            val result = scanForAttachment(chain, checkers, allowedContainerChecker)
            if (result != null)
                return result
            chain.removeLast()
        }
        return null
    }

    data class ScanResult(
        val content: AttachmentContent,
        val chain: List<AttachmentContainer>,
    )

    fun interface Checker<T : AttachmentContent> {
        fun check(obj: Attachment): T?
    }
}