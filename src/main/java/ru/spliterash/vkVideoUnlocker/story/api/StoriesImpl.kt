package ru.spliterash.vkVideoUnlocker.story.api

import com.fasterxml.jackson.core.type.TypeReference
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryNotFoundById
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.api.VideosImpl
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.readResponse
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkItemsResponse

@Prototype
class StoriesImpl(
    @Parameter private val client: OkHttpClient,
    private val vkHelper: VkHelper
) : Stories {
    override suspend fun getById(storyId: String): VkStory {
        return VkConst
            .requestBuilder()
            .header("user-agent", VideosImpl.USER_AGENT)
            .get()
            .url(
                VkConst.urlBuilder("stories.getById")
                    .addQueryParameter("stories", storyId)
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(vkHelper, object : TypeReference<VkItemsResponse<VkStory>>() {})
            .items
            .firstOrNull()
            ?: throw StoryNotFoundById()
    }
}