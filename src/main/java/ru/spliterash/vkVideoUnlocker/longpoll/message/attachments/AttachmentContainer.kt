package ru.spliterash.vkVideoUnlocker.longpoll.message.attachments

import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment

interface AttachmentContainer {
    fun containers(): List<AttachmentContainer>
    fun attachments(): List<Attachment>
}