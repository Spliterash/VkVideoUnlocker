package ru.spliterash.vkVideoUnlocker.video.impl

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.video.Video
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoEmptyUrlException

@Singleton
class VideoAccessorFactory(
    factory: OkHttpFactory
) {
    private val client = factory.create().build()

    fun create(video: Video): VideoAccessor {
        val url = video.url ?: throw VideoEmptyUrlException()
        return VideoAccessorImpl(client, url)
    }
}