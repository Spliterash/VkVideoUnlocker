package ru.spliterash.vkVideoUnlocker.message.impl

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.message.Messages
import ru.spliterash.vkVideoUnlocker.message.vkModels.Forward
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkConst

@Prototype
class MessagesImpl(
    @Parameter private val client: OkHttpClient,
    private val vkHelper: VkHelper,
    private val mapper: ObjectMapper
) : Messages {
    override suspend fun sendMessage(peerId: Int, message: String?, replyTo: Int?, attachments: String?): Int {
        val request = VkConst
            .requestBuilder()
            .url(
                VkConst.urlBuilder("messages.send")
                    .addQueryParameter("random_id", "0")
                    .addQueryParameter("peer_id", peerId.toString())
                    .addQueryParameter("disable_mentions", "true")
                    .apply {
                        if (message != null)
                            addQueryParameter("message", message)
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
        val response = client
            .newCall(request)
            .executeAsync()

        return vkHelper.readResponse(response, Int::class.java).first
    }
}