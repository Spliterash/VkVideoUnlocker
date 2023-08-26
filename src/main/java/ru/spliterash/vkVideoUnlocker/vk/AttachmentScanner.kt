package ru.spliterash.vkVideoUnlocker.vk

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent

@Singleton
class AttachmentScanner {
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