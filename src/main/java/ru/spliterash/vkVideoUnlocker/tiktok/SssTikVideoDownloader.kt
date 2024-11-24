package ru.spliterash.vkVideoUnlocker.tiktok

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import ru.spliterash.vkVideoUnlocker.common.InfoLoaderService
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.accessor.UrlVideoAccessorImpl
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import java.util.regex.Pattern

@Singleton
class SssTikVideoDownloader(
    okHttpFactory: OkHttpFactory,
    private val objectMapper: ObjectMapper,
    private val infoLoaderService: InfoLoaderService
) : TiktokVideoDownloader {
    private val client = okHttpFactory.create()
        .build()

    private val ttPattern = Pattern.compile("s_tt = '(?<tt>.*?)'")
    private val onClickPattern = Pattern.compile("downloadX\\('(.*?)'\\)")

    override suspend fun download(videoUrl: String): VideoAccessor {
        val tt = getTt()
        val downloadXUrl = acquireDownloadXUrl(videoUrl, tt)
        val downloadUrl = transformXDownload(downloadXUrl)

        return UrlVideoAccessorImpl(infoLoaderService, client, downloadUrl)
    }

    private suspend fun getTt(): String {
        val response = Request.Builder()
            .url("https://ssstik.io/")
            .get()
            .build()
            .executeAsync(client)
            .use { it.body.string() }

        val matcher = ttPattern.matcher(response)
        if (!matcher.find()) throw IllegalStateException("Ssstik error 1: fail to parse tt")

        return matcher.group("tt")
    }

    private suspend fun acquireDownloadXUrl(url: String, tt: String): DownloadXUrlResult {
        val request = Request.Builder()
            .url("https://ssstik.io/abc?url=dl")
            .post(
                FormBody.Builder()
                    .add("id", url)
                    .add("locale", "en")
                    .add("tt", tt)
                    .build()
            )
            .setFirefox()
            .build()
        val response = request
            .executeAsync(client)
            .use { it.body.string() }

        if (response.isBlank()) throw IllegalStateException("Ssstik error 2: empty response")
        val parsed = Jsoup.parse(response)
        val hdButton = parsed.select("#hd_download").first()
            ?: throw IllegalStateException("Ssstik error 3: no hd_download element found")

        val onClick = hdButton.attr("onclick")
        val matcher = onClickPattern.matcher(onClick)
        if (!matcher.find()) throw IllegalStateException("Ssstik error 4: invalid onClick content: $onClick")

        val xUrl = "https://ssstik.io" + matcher.group(1)
        val input = parsed.select("input[name=tt]").first()
            ?: throw IllegalStateException("Sstik error 5: no hidden tt input found")

        return DownloadXUrlResult(
            xUrl,
            input.attr("value")
        )

    }

    private data class DownloadXUrlResult(
        val url: String,
        val newTT: String
    )

    private suspend fun transformXDownload(downloadXUrl: DownloadXUrlResult): String {
        val finalUrl = Request
            .Builder()
            .url(downloadXUrl.url)
            .setFirefox()
            .post(
                FormBody.Builder()
                    .add("tt", downloadXUrl.newTT)
                    .build()
            )
            .header("HX-Request", "true")
            .header("HX-Trigger", "hd_download")
            .header("HX-Target", "hd_download")
            .header("HX-Current-URL", "https://ssstik.io/")
            .build()
            .executeAsync(client)
            .use { it.headers["hx-redirect"] }

        if (finalUrl == null) throw IllegalStateException("Ssstik error 6: no hx-redirect header found")


        return finalUrl
    }


    private fun Request.Builder.setFirefox() =
        addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0")
}