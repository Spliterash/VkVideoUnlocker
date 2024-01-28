package ru.spliterash.vkVideoUnlocker.vk

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.Message
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent
import java.util.function.Predicate

@Singleton
class MessageScanner {
    fun <T> scanForText(message: Message, func: (String) -> T?): T? {
        val text = message.text
        if (text != null) {
            val result = func(text)
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
        return scanForAttachment(root, listOf(checker)) as T?
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun scanForAttachment(
        root: AttachmentContainer,
        checkers: List<Checker<*>>,
        allowedContainerChecker: Predicate<AttachmentContainer> = Predicate { true }
    ): AttachmentContent? {
        if (!allowedContainerChecker.test(root)) return null
        for (attachment in root.attachments()) {
            for (checker in checkers) {
                val needle = checker.check(attachment)
                if (needle != null)
                    return needle
            }

            if (attachment.wall != null) {
                val scanResult = scanForAttachment(attachment.wall, checkers, allowedContainerChecker)
                if (scanResult != null)
                    return scanResult
            }

            if (attachment.wallReply != null) {
                val scanResult = scanForAttachment(attachment.wallReply, checkers, allowedContainerChecker)
                if (scanResult != null)
                    return scanResult
            }
        }
        for (somethingWithAttachments in root.containers()) {
            val result = scanForAttachment(somethingWithAttachments, checkers, allowedContainerChecker)
            if (result != null)
                return result
        }

        return null
    }


    fun interface Checker<T : AttachmentContent> {
        fun check(obj: Attachment): T?
    }
}