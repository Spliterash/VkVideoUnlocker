package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.accessor.UrlVideoAccessorImpl
import java.net.URL
import java.util.regex.Pattern

//@Singleton
class TikCdnDownloader(
    okHttpFactory: OkHttpFactory
) : TiktokDownloader {
    private val client = okHttpFactory.create().build()
    private val tikCdnTokenPattern = Pattern.compile("s_tt += +'(?<token>[a-zA-Z0-9_-]+)'")
    private val tikCdnVideoUrlPattern = Pattern.compile("https://tikcdn\\.io/ssstik/(?<id>\\d+)")
    override suspend fun download(videoUrl: String): TiktokVideo {
        val token = getActualToken()

        val response = Request.Builder()
            .url("https://ssstik.io/abc?url=dl")
            .post(
                FormBody.Builder()
                    .addEncoded("id", videoUrl)
                    .addEncoded("locale", "en")
                    .addEncoded("tt", token)
                    .build()
            )
            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")
            .build()
            .executeAsync(client)
            .body
            .string()
        val matcher = tikCdnVideoUrlPattern.matcher(response)
        if (!matcher.find()) {
            println(response)
            throw IllegalStateException("ssstik.io return wrong response")
        }

        val url = matcher.group()
        val id = matcher.group("id")

        return TiktokVideo(id, UrlVideoAccessorImpl(client, URL(url)))

    }

    private suspend fun getActualToken(): String {
        val response = client
            .newCall(Request.Builder().get().url("https://ssstik.io/en").build())
            .executeAsync()

        val page = response.body.string()
        val matcher = tikCdnTokenPattern.matcher(page)
        if (!matcher.find()) throw IllegalStateException("Fail to parse tikcdn token")

        return matcher.group("token")
    }
}