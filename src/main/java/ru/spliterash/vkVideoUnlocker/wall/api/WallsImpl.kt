package ru.spliterash.vkVideoUnlocker.wall.api

import com.fasterxml.jackson.core.type.TypeReference
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkConst
import ru.spliterash.vkVideoUnlocker.wall.exceptions.WallPostNotFoundException

@Prototype
class WallsImpl(
    @Parameter private val client: OkHttpClient,
    private val vkHelper: VkHelper
) : Walls {
    override suspend fun getById(id: String): SomethingWithAttachments {
        val response = VkConst
            .requestBuilder()
            .get()
            .url(
                VkConst.urlBuilder("wall.getById")
                    .addQueryParameter("posts", id)
                    .build()
            )
            .build()
            .executeAsync(client)

        val list = vkHelper.readResponse(response, object : TypeReference<List<SomethingWithAttachments>>() {})

        return list.firstOrNull() ?: throw WallPostNotFoundException()
    }
}