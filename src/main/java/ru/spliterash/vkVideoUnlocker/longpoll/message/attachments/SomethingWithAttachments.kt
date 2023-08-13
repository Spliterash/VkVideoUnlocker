package ru.spliterash.vkVideoUnlocker.longpoll.message.attachments

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment

data class SomethingWithAttachments(
    @JsonProperty("attachments")
    val attachments: List<Attachment>
) : AttachmentContainer {
    override fun containers(): List<AttachmentContainer> = listOf()

    override fun attachments(): List<Attachment> = attachments
}
