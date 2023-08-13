package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonProperty

data class FwdMessage(
    @JsonProperty("text") override val text: String?,
    @JsonProperty("attachments") override val attachments: List<Attachment>,
    @JsonProperty("fwd_messages") override val fwdMessages: List<FwdMessage> = listOf(),
    @JsonProperty("reply_message") override val replyMessage: FwdMessage?,
) : Message