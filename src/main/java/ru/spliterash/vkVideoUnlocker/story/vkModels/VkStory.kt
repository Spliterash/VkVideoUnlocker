package ru.spliterash.vkVideoUnlocker.story.vkModels

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

data class VkStory(
    val id: Int,
    @JsonProperty("is_expired")
    val isExpired: Boolean,
    @JsonProperty("can_see")
    val canSee: Boolean?,
    val type: Type?,
    val video: VkVideo?
) : AttachmentContent {
    enum class Type {
        @JsonProperty("video")
        VIDEO,

        @JsonProperty("photo")
        PHOTO,

        @JsonEnumDefaultValue
        ANOTHER
    }
}
