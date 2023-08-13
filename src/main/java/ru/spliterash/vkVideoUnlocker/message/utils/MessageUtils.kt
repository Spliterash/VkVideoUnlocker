package ru.spliterash.vkVideoUnlocker.message.utils

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.ISomethingWithAttachments

@Singleton
class MessageUtils {
    fun <T> scanForAttachment(root: ISomethingWithAttachments, checker: Checker<T>): T? {
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
        for (somethingWithAttachments in root.innerSomething()) {
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