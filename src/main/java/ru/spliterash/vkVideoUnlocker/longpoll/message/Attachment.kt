package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.VkLink
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.Wall
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

data class Attachment(
    @JsonProperty("video") val video: VkVideo?,
    @JsonProperty("wall") val wall: Wall?,
    @JsonProperty("wall_reply") val wallReply: SomethingWithAttachments?,
    @JsonProperty("story") val story: VkStory?,
    @JsonProperty("link") val link: VkLink?,
    @JsonProperty("type") val type: Type?
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

        @JsonProperty("link")
        LINK,

        @JsonEnumDefaultValue
        UNKNOWN
    }
}