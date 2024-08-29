package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.executeAsync
import org.jsoup.Jsoup
import ru.spliterash.vkVideoUnlocker.common.InfoLoaderService
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import java.util.regex.Pattern

@Singleton
class TikCdnPhotoDownloader(
    okHttpFactory: OkHttpFactory,
    private val infoLoaderService: InfoLoaderService,
) : TiktokPhotoDownloader {
    private val client = okHttpFactory.create().build()
    private val tikCdnTokenPattern = Pattern.compile("s_tt += +'(?<token>[a-zA-Z0-9_-]+)'")
    override suspend fun download(url: String): TiktokPhoto {
        val token = getActualToken()

        val response = Request.Builder()
            .url("https://ssstik.io/abc?url=dl")
            .post(
                FormBody.Builder()
                    .addEncoded("id", url)
                    .addEncoded("locale", "en")
                    .addEncoded("tt", token)
                    .build()
            )
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")
            .build()
            .executeAsync(client)
            .body
            .string()

        val page = Jsoup.parse(response)
        val slides = page.select(".splide .download_link").map { slideHref ->
            slideHref.attr("href").loadLink()
        }
        val musicLink = page.selectFirst(".download_link.music")
        val music = musicLink?.attr("href")?.loadLink()

        return TiktokPhoto(music, slides)
    }

    private suspend fun String.loadLink() = InputStreamSource { infoLoaderService.load(this) }

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