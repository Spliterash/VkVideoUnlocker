package ru.spliterash.vkVideoUnlocker.common.okHttp

import jakarta.inject.Singleton
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

@Singleton
class OkHttpFactory(
    private val pool: ConnectionPool,
    private val dispatcher: Dispatcher,
) {
    fun create() = OkHttpClient.Builder()
        .connectionPool(pool)
        .dispatcher(dispatcher)
}