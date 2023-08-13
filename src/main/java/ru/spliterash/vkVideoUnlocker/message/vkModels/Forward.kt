package ru.spliterash.vkVideoUnlocker.message.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Forward object
 */
data class Forward(
    @JsonProperty("peer_id")
    val peerId: Int,

    @JsonProperty("conversation_message_ids")
    val conversationMessageIds: List<Int>,
    @JsonProperty("is_reply")
    val isReply: Boolean,
)