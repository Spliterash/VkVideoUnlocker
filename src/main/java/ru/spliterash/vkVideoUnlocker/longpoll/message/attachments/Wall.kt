package ru.spliterash.vkVideoUnlocker.longpoll.message.attachments

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.Attachment

data class Wall(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("owner_id")
    val ownerId: Long,
    @JsonProperty("attachments")
    val attachments: List<Attachment> = listOf(),
    @JsonProperty("copy_history")
    val copyHistory: List<Wall> = listOf(),
) : AttachmentContainer, AttachmentContent {
    override fun containers(): List<AttachmentContainer> = copyHistory

    override fun attachments(): List<Attachment> = attachments
}