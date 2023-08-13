package ru.spliterash.vkVideoUnlocker.group.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import okhttp3.executeAsync
import org.slf4j.LoggerFactory
import ru.spliterash.vkVideoUnlocker.group.dto.GroupInfo
import ru.spliterash.vkVideoUnlocker.group.vkModels.LongPollServerResponse
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
        TODO("Not yet implemented")
    }

    override suspend fun join(groupId: Int) {
        TODO("Not yet implemented")
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