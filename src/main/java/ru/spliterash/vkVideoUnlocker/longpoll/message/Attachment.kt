package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

data class Attachment(
    @JsonProperty("video") val video: VkVideo?,
    @JsonProperty("wall") val wall: SomethingWithAttachments?,
    @JsonProperty("wall_reply") val wallReply: SomethingWithAttachments?,
    @JsonProperty("story") val story: VkStory?,
    @JsonProperty("type") val type: Type
) {
    enum class Type {
        @JsonProperty("video")
        VIDEO,

        @JsonProperty("wall")
        WALL,

        @JsonProperty("wall_reply")
        WALL_REPLY,

        @JsonProperty("story")
        STORY,

        @JsonEnumDefaultValue
        UNKNOWN
    }
}