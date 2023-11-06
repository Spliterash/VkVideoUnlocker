package ru.spliterash.vkVideoUnlocker.message.vkModels.request

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Forward object
 */
data class Forward(
    @JsonProperty("peer_id")
    val peerId: Long,

    @JsonProperty("conversation_message_ids")
    val conversationMessageIds: List<Long>,
    @JsonProperty("is_reply")
    val isReply: Boolean,
)