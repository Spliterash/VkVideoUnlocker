package ru.spliterash.vkVideoUnlocker.group.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import okhttp3.executeAsync
import org.slf4j.LoggerFactory
import ru.spliterash.vkVideoUnlocker.group.dto.GroupInfo
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.group.dto.MemberStatus
import ru.spliterash.vkVideoUnlocker.group.vkModels.LongPollServerResponse
import ru.spliterash.vkVideoUnlocker.group.vkModels.VkGroupGetByIdResponse
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.actor.types.Actor
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkConst

@Prototype
class GroupsImpl(
    @Parameter private val client: OkHttpClient,
    @Parameter private val actor: Actor,

    private val vkHelper: VkHelper,
    private val mapper: ObjectMapper
) : Groups {
    companion object {
        private val log = LoggerFactory.getLogger(GroupsImpl::class.java)
    }

    override suspend fun status(groupId: Int): GroupInfo {
        val request = VkConst.requestBuilder()
            .url(
                VkConst.urlBuilder("groups.getById") // Hidden method, taken from mobile app
                    .addQueryParameter("group_id", groupId.toString())
                    .addQueryParameter("fields", "member_status,is_closed")
                    .build()

            )
            .build()

        val response = client
            .newCall(request)
            .executeAsync()
        val (mapped) = vkHelper.readResponse(response, VkGroupGetByIdResponse::class.java)
        val status = when (mapped.isClosed) {
            VkGroupGetByIdResponse.ClosedStatus.OPEN -> GroupStatus.PUBLIC
            VkGroupGetByIdResponse.ClosedStatus.CLOSED -> GroupStatus.CLOSE
            VkGroupGetByIdResponse.ClosedStatus.PRIVATE -> GroupStatus.PRIVATE
        }
        val member = when (mapped.memberStatus) {
            VkGroupGetByIdResponse.MemberStatus.NOT_MEMBER -> MemberStatus.NO
            VkGroupGetByIdResponse.MemberStatus.MEMBER -> MemberStatus.MEMBER
            VkGroupGetByIdResponse.MemberStatus.DO_NOT_SURE -> MemberStatus.MEMBER
            VkGroupGetByIdResponse.MemberStatus.DECLINED_INVITE -> MemberStatus.NO
            VkGroupGetByIdResponse.MemberStatus.REQUEST_SEND -> MemberStatus.REQUEST_SEND
            VkGroupGetByIdResponse.MemberStatus.INVITED -> MemberStatus.NO
        }

        return GroupInfo(
            status,
            member
        )
    }

    override suspend fun join(groupId: Int) {
        val request = VkConst.requestBuilder()
            .url(
                VkConst.urlBuilder("groups.join")
                    .addQueryParameter("group_id", groupId.toString())
                    .build()

            )
            .build()

        val response = client
            .newCall(request)
            .executeAsync()
        // Обработка ошибок если будут
        vkHelper.readResponse(response, Void::class.java)
    }

    override suspend fun getLongPollServer(): LongPollServerResponse {
        val request = VkConst.requestBuilder()
            .url(
                VkConst.urlBuilder("groups.getLongPollServer")
                    .addQueryParameter("group_id", actor.id.toString())
                    .build()
            )
            .build()
        val response = client
            .newCall(request)
            .executeAsync()

        return vkHelper.readResponse(response, LongPollServerResponse::class.java).first
    }
}