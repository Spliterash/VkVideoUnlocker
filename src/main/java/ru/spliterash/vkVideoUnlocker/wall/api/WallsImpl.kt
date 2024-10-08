package ru.spliterash.vkVideoUnlocker.wall.api

import com.fasterxml.jackson.core.type.TypeReference
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.video.api.VideosImpl
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkItemsResponse
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
            .header("user-agent", VideosImpl.USER_AGENT)
            .url(
                VkConst.urlBuilder("wall.getById")
                    .addQueryParameter("posts", id)
                    .addQueryParameter("extended", "1")
                    .addQueryParameter("fields", "video_files")
                    .build()
            )
            .build()
            .executeAsync(client)

        val items = vkHelper.readResponse(
            response,
            object : TypeReference<VkItemsResponse<SomethingWithAttachments>>() {}
        )

        return items.items.firstOrNull() ?: throw WallPostNotFoundException()
    }
}