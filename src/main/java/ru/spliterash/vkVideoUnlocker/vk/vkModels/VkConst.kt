package ru.spliterash.vkVideoUnlocker.vk.vkModels

import okhttp3.HttpUrl
import okhttp3.Request

object VkConst {
    const val HOST = "api.vk.com"
    const val START = "https://$HOST/method/"
    const val VERSION = "5.131"

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