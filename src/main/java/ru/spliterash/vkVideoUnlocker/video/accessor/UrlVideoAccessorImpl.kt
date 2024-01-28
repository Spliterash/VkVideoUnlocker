package ru.spliterash.vkVideoUnlocker.video.accessor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync
import java.net.URL

class UrlVideoAccessorImpl(
    private val client: OkHttpClient,
    private val url: URL
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

    override suspend fun load(): VideoAccessor.Info {
        val response = client
            .newCall(
                Request.Builder()
                    .url(url)
                    .get()
                    .build()
            )
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
}