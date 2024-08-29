package ru.spliterash.vkVideoUnlocker.video.accessor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.common.InfoLoaderService
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import java.net.URL

class UrlVideoAccessorImpl(
    private val infoLoaderService: InfoLoaderService,
    private val client: OkHttpClient,
    private val url: String
) : VideoAccessor {
    override suspend fun size(quality: Int): Long {
        val response = client
            .newCall(
                Request.Builder()
                    .url(url)
                    .head()
                    .build()
            )
            .executeAsync()

        return response.headers["Content-Length"]?.toLong() ?: -1L
    }

    override suspend fun load(): InputStreamSource.Info {
        return infoLoaderService.load(url)
    }
}