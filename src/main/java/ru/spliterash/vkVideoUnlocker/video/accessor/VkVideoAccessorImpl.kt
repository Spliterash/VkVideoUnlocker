package ru.spliterash.vkVideoUnlocker.video.accessor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import org.apache.commons.logging.LogFactory
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.video.api.VideosImpl
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import java.net.URL


class VkVideoAccessorImpl(
    private val client: OkHttpClient,
    private val video: VkVideo,
) : VideoAccessor, AdvancedVideoAccessor {
    override val maxQuality: Int
    override val maxQualityUrl: URL
    override fun preview(): URL = video.preview()

    init {
        val (quality, url) = video.maxQuality()

        maxQuality = quality;
        maxQualityUrl = url
    }

    override suspend fun size(quality: Int): Long {
        val request = builder(video.qualityUrl(quality))
            .head()
            .build()

        val response = client
            .newCall(request)
            .executeAsync()

        return response.headers["Content-Length"]?.toLong() ?: -1L
    }

    override suspend fun load() = load(maxQuality)

    override suspend fun load(quality: Int): InputStreamSource.Info {
        val request = builder(video.qualityUrl(quality))
            .get()
            .build()

        val response = client.newCall(request)
            .executeAsync()
        val size = response.headers["Content-Length"]?.toLong() ?: -1L


        return InputStreamSource.Info(
            response.code,
            response
                .body
                .byteStream(),
            null,
            size
        )
    }

    override suspend fun load(quality: Int, range: String): InputStreamSource.Info {
        val url = video.qualityUrl(quality)

        val request = builder(url)
            .get()
            .header("Range", range)
            .build()

        val response = client.newCall(request)
            .executeAsync()
        val size = response.headers["Content-Length"]?.toLong() ?: -1L
        val contentRange = response.headers["Content-Range"]

        return InputStreamSource.Info(
            response.code,
            response
                .body
                .byteStream(),
            contentRange,
            size
        )
    }

    private fun builder(url: URL): Request.Builder {
        val userAgent = VideosImpl.USER_AGENT
        return Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                userAgent
            )
    }

    companion object {
        private val log = LogFactory.getLog(VkVideoAccessorImpl::class.java)
    }
}