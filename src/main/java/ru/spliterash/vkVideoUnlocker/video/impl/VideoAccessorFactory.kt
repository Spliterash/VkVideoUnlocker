package ru.spliterash.vkVideoUnlocker.video.impl

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoEmptyUrlException

@Singleton
class VideoAccessorFactory(
    factory: OkHttpFactory
) {
    private val client = factory.create().build()

    fun create(video: VkVideo): VideoAccessor {
        val url = video.extractUrl()
        return VideoAccessorImpl(client, url)
    }
}