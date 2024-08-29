package ru.spliterash.vkVideoUnlocker.common

import jakarta.inject.Singleton
import okhttp3.Request
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory

@Singleton
class InfoLoaderService(
    factory: OkHttpFactory
) {
    private val client = factory.create().build()
    suspend fun load(url: String, range: String? = null): InputStreamSource.Info {
        val request = builder(url)
            .get()
            .apply { if (range != null) header("Range", range) }
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

    fun builder(url: String) = Request.Builder()
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
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"
        )
}