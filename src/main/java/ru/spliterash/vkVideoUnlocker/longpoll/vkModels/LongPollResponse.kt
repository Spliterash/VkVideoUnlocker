package ru.spliterash.vkVideoUnlocker.longpoll.vkModels

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class LongPollResponse(
    val ts: String?,
    val failed: Int?,
    val updates: List<Update> = listOf()
) {
    data class Update(
        val type: String,
        @JsonProperty("event_id") val eventId: String,
        @JsonProperty("object") val body: JsonNode
    )
}