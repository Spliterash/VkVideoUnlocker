package ru.spliterash.vkVideoUnlocker.common

import jakarta.inject.Singleton
import okhttp3.Request
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory

@Singleton
class RedirectHelper(
    private val okHttpFactory: OkHttpFactory
) {
    private val client = okHttpFactory.create().followRedirects(false).build()

    suspend fun finalUrl(startUrl: String): String {
        var loop = 0
        var currentUrl = startUrl
        while (loop < 50) {
            val response = client.newCall(
                Request.Builder()
                    .url(currentUrl)
                    .head()
                    .build()
            )
                .executeAsync()

            if (response.isRedirect)
                currentUrl = response.header("Location")!!
            else {
                return currentUrl
            }
            loop++
        }
        throw RuntimeException("Too much redirects")
    }
}