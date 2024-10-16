package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonProperty

data class ReplyMessage(
    @JsonProperty("text") override val text: String?,
    @JsonProperty("attachments") override val attachments: List<Attachment>,
    @JsonProperty("fwd_messages") override val fwdMessages: List<FwdMessage> = listOf(),
    @JsonProperty("reply_message") override val replyMessage: ReplyMessage?,
    @JsonProperty("from_id") override val fromId: Long,
) : Message