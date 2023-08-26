package ru.spliterash.vkVideoUnlocker.vk.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import ru.spliterash.vkVideoUnlocker.vk.actor.types.Actor
import ru.spliterash.vkVideoUnlocker.vk.VkConst

class VkInterceptor(private val actor: Actor) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain.request()
            .url
            .newBuilder()
            .addQueryParameter("access_token", actor.token)
            .addQueryParameter("v", VkConst.VERSION)
            .build()
        val newRequest = chain
            .request()
            .newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}