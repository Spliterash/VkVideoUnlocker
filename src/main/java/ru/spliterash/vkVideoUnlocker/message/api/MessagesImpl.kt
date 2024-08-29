package ru.spliterash.vkVideoUnlocker.message.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Forward
import ru.spliterash.vkVideoUnlocker.message.vkModels.response.MessageSendResponse
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper

@Prototype
class MessagesImpl(
    @Parameter private val client: OkHttpClient,
    private val vkHelper: VkHelper,
    private val mapper: ObjectMapper
) : Messages {
    private fun String.normalizeLength() = if (length > 4096) substring(
        0,
        4096 - 3
    ) + "..." else
        this


    override suspend fun sendMessage(peerId: Long, message: String?, replyTo: Long?, attachments: String?): Long {
        val response = VkConst
            .requestBuilder()
            .url(
                VkConst.urlBuilder("messages.send")
                    .addQueryParameter("random_id", "0")
                    .addQueryParameter("peer_ids", peerId.toString())
                    .addQueryParameter("disable_mentions", "1")
                    .apply {
                        if (message != null)
                            addQueryParameter("message", message.normalizeLength())
                        if (attachments != null)
                            addQueryParameter("attachment", attachments)
                        if (replyTo != null) {
                            val forward = Forward(peerId, listOf(replyTo), true)
                            val string = mapper.writeValueAsString(forward)
                            addQueryParameter("forward", string)
                        }
                    }
                    .build()
            )
            .build()
            .executeAsync(client)

        val result = vkHelper.readResponse(response, object : TypeReference<List<MessageSendResponse>>() {}).first()
        if (result.error != null) throw RuntimeException(result.error)

        return result.conversationMessageId!!
    }

    override suspend fun editMessage(
        peerId: Long,
        conversationMessageId: Long,
        message: String?,
        attachments: String?
    ) {
        val response = VkConst
            .requestBuilder()
            .url(
                VkConst.urlBuilder("messages.edit")
                    .addQueryParameter("peer_id", peerId.toString())
                    .addQueryParameter("conversation_message_id", conversationMessageId.toString())
                    .addQueryParameter("disable_mentions", "1")
                    .addQueryParameter("keep_forward_messages", "1")
                    .apply {
                        if (message != null)
                            addQueryParameter("message", message.normalizeLength())
                        if (attachments != null)
                            addQueryParameter("attachment", attachments)
                    }
                    .build()
            )
            .build()
            .executeAsync(client)

        vkHelper.readResponse(response, Int::class.java)
    }
}