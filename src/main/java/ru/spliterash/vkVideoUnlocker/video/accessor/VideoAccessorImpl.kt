package ru.spliterash.vkVideoUnlocker.video.accessor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import org.apache.commons.logging.LogFactory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import java.net.URL


class VideoAccessorImpl(
    private val client: OkHttpClient,
    private val video: VkVideo,
) : VideoAccessor {
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

    override suspend fun load(quality: Int): VideoAccessor.Info {
        val request = builder(video.qualityUrl(quality))
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

    override suspend fun load(quality: Int, range: String): VideoAccessor.Info {
        val url = video.qualityUrl(quality)

        val request = builder(url)
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

    private fun builder(url: URL): Request.Builder {
        val userAgent = if (url.query.contains("srcAg=GECKO"))
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0"
        else
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"
        return Request.Builder()
            .url(url)
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
            )
            .header(
                "Accept-Encoding",
                "gzip, deflate, br"
            )
            .header(
                "User-Agent",
                userAgent
            )
    }

    companion object {
        private val log = LogFactory.getLog(VideoAccessorImpl::class.java)
    }
}