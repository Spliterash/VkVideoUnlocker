package ru.spliterash.vkVideoUnlocker.video.impl

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor

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