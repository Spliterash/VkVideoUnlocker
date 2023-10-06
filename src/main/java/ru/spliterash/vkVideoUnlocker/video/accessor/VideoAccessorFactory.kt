package ru.spliterash.vkVideoUnlocker.video.accessor

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

@Singleton
class VideoAccessorFactory(
    factory: OkHttpFactory
) {
    private val client = factory.create().build()

    fun create(video: VkVideo): VideoAccessor {
        return VideoAccessorImpl(client, video)
    }
}