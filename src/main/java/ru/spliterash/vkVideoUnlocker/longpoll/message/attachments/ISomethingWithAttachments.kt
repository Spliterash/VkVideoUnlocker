package ru.spliterash.vkVideoUnlocker.longpoll.message.attachments

import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment

interface ISomethingWithAttachments {
    fun innerSomething(): List<ISomethingWithAttachments>
    fun attachments(): List<Attachment>
}