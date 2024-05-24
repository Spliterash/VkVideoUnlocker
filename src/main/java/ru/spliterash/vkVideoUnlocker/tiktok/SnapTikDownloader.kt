package ru.spliterash.vkVideoUnlocker.tiktok

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import okhttp3.MultipartBody
import okhttp3.Request
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.accessor.UrlVideoAccessorImpl
import java.net.URL
import java.util.regex.Pattern

// Код деобфускации спизжен отсюда: https://github.com/Ty3uK/snaptik-bot/blob/main/src/url_resolver/snap/util.rs
@Singleton
class SnapTikDownloader(
    okHttpFactory: OkHttpFactory,
    private val objectMapper: ObjectMapper
) : TiktokDownloader {
    private val client = okHttpFactory.create()
        .build()

    private val tokenPattern = Pattern.compile("<input name=\"token\" value=\"(.*?)\"")
    private val jsDecryptPattern = Pattern.compile("\\(\"(.+?)\",(\\d+),\"(.+?)\",(\\d+),(\\d+),(\\d+)\\)")
    private val hdTokenPattern = Pattern.compile("data-tokenhd=\\\\\"(.*?)\\\\\"")
    private val numberPattern =
        Pattern.compile("https://www\\.tiktok\\.com/oembed\\?url=https://www\\.tiktok\\.com/@tiktok/video/(\\d+)")

    override suspend fun download(videoUrl: String): TiktokVideo {
        val token = getToken()

        val encryptedJs = Request.Builder()
            .url("https://snaptik.app/abc2.php")
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("url", videoUrl)
                    .addFormDataPart("lang", "en")
                    .addFormDataPart("token", token)
                    .build()
            )
            .build()
            .executeAsync(client)
            .body
            .string()
        val matcher = jsDecryptPattern.matcher(encryptedJs)
        if (!matcher.find()) throw IllegalStateException("snaptik stage 2 error: regex encrypt js")
        val decodedJs = try {
            decryptJs(
                matcher.group(1),
                matcher.group(2).toInt(),
                matcher.group(3),
                matcher.group(4).toInt(),
                matcher.group(5).toInt(),
                matcher.group(6).toInt()
            )
        } catch (ex: Exception) {
            throw IllegalStateException("snaptik stage 3 error: decrypt js")
        }
        val numberMatcher = numberPattern.matcher(decodedJs)
        if (!numberMatcher.find()) throw IllegalStateException("Failed to parse video url from snaptik response")
        val number = numberMatcher.group(1)
        val hdTokenMatcher = hdTokenPattern.matcher(decodedJs)
        if (!hdTokenMatcher.find()) throw IllegalStateException("snaptik stage 4 error: extract hd token, decrypted response: $decodedJs")
        val hdToken = hdTokenMatcher.group(1)
        val hdTokenLinkGetter = "https://snaptik.app/getHdLink.php?token=$hdToken"
        val linkResponse = Request.Builder()
            .get()
            .url(hdTokenLinkGetter)
            .build()
            .executeAsync(client)
            .body
            .string()
        val node = objectMapper.readTree(linkResponse)
        val url = node.get("url").asText()

        return TiktokVideo(number, UrlVideoAccessorImpl(client, URL(url)))
    }

    private suspend fun getToken(): String {
        val response = Request.Builder()
            .url("https://snaptik.app")
            .get()
            .build()
            .executeAsync(client)
            .body
            .string()
        val matcher = tokenPattern.matcher(response)
        if (!matcher.find()) throw IllegalStateException("snaptik stage 1 error: token extract")

        return matcher.group(1)
    }


    private fun decryptJs(h: String, _u: Int, n: String, t: Int, e: Int, _r: Int): String {
        val result = StringBuilder()

        val hChars = h.toCharArray()
        val nChars = n.toCharArray()

        var i = 0
        while (i < h.length) {
            var s = ""

            while (hChars[i] != nChars[e]) {
                s += hChars[i]
                i++
            }

            var j = 0
            while (j < n.length) {
                s = s.replace(nChars[j].toString(), j.toString())
                j++
            }

            i++

            val charCode = s.toLong(e) - t.toLong()
            val char = charCode.toInt().toChar()

            result.append(char)
        }

        return result.toString()
    }

}