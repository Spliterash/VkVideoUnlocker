package ru.spliterash.vkVideoUnlocker.message.vkModels.response

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageSendResponse(
    @JsonProperty("peer_id")
    val peerId: Long?,
    @JsonProperty("message_id")
    val messageId: Long?,
    @JsonProperty("conversation_message_id")
    val conversationMessageId: Long?,
    val error: String?
)