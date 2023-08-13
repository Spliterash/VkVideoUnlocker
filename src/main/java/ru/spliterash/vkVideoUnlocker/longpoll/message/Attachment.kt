package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo

data class Attachment(
    @JsonProperty("video") val video: VkVideo?,
    @JsonProperty("wall") val wall: SomethingWithAttachments?,
    @JsonProperty("wall_reply") val wallReply: SomethingWithAttachments?,
    @JsonProperty("type") val type: Type
) {
    enum class Type {
        @JsonProperty("video")
        VIDEO,

        @JsonProperty("wall")
        WALL,

        @JsonProperty("wall_reply")
        WALL_REPLY,
    }
}