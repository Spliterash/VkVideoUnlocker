package ru.spliterash.vkVideoUnlocker.group.vkModels

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class VkGroupGetByIdResponse(
    @JsonProperty("is_subscribed") val isSubscribed: Boolean,
    @JsonProperty("member_status") val memberStatus: MemberStatus,
    @JsonProperty("is_closed") val isClosed: ClosedStatus
) {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    enum class MemberStatus {
        NOT_MEMBER,
        MEMBER,
        DO_NOT_SURE,
        DECLINED_INVITE,
        REQUEST_SEND,
        INVITED
    }

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    enum class ClosedStatus {
        OPEN,
        CLOSED,
        PRIVATE
    }
}