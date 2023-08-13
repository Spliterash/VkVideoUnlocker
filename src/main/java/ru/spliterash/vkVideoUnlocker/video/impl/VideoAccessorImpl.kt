package ru.spliterash.vkVideoUnlocker.video.impl

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import org.apache.commons.logging.LogFactory
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import java.net.URL


class VideoAccessorImpl(
    private val client: OkHttpClient,
    private val url: URL,
) : VideoAccessor {
    override suspend fun size(): Long {
        val request = builder()
            .head()
            .build()

        val response = client
            .newCall(request)
            .executeAsync()

        return response.headers["Content-Length"]?.toLong() ?: -1L
    }

    override suspend fun load(): VideoAccessor.Info {
        val request = builder()
            .get()
            .build()

        val response = client.newCall(request)
            .executeAsync()
        val size = response.headers["Content-Length"]?.toLong() ?: -1L


        return VideoAccessor.Info(
            response.code,
            response
                .body
                .byteStream(),
            null,
            size
        )
    }

    override suspend fun load(range: String): VideoAccessor.Info {
        val request = builder()
            .get()
            .header("Range", range)
            .build()

        val response = client.newCall(request)
            .executeAsync()
        val size = response.headers["Content-Length"]?.toLong() ?: -1L
        val contentRange = response.headers["Content-Range"]

        return VideoAccessor.Info(
            response.code,
            response
                .body
                .byteStream(),
            contentRange,
            size
        )
    }

    private fun builder(): Request.Builder = Request.Builder().url(url)
    fun finalize() {
        log.info("Accessor finalized for video $url")
    }

    companion object {
        private val log = LogFactory.getLog(VideoAccessorImpl::class.java)
    }
}