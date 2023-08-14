package ru.spliterash.vkVideoUnlocker.common.okHttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync

suspend fun Request.executeAsync(client: OkHttpClient) = client
    .newCall(this)
    .executeAsync()