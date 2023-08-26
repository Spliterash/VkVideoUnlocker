package ru.spliterash.vkVideoUnlocker.vk

import okhttp3.HttpUrl
import okhttp3.Request
import java.util.regex.Pattern

object VkConst {
    const val HOST = "api.vk.com"
    const val START = "https://$HOST/method/"
    const val VERSION = "5.131"
    val VK_ATTACHMENT_PATTERN: Pattern = Pattern.compile("(?<type>video|wall|story)(?<owner>-?\\d+)_(?<id>\\d+)")

    fun urlBuilder(method: String): HttpUrl.Builder {
        return HttpUrl.Builder()
            .scheme("https")
            .host(HOST)
            .addPathSegment("method")
            .addPathSegment(method)
    }

    fun url(method: String): HttpUrl {
        return urlBuilder(method).build()
    }

    fun requestBuilder(): Request.Builder {
        return Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-encoded")
    }
}