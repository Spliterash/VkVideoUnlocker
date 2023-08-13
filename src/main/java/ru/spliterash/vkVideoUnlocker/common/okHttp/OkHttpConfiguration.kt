package ru.spliterash.vkVideoUnlocker.common.okHttp

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import okhttp3.ConnectionPool
import okhttp3.Dispatcher

@Factory
class OkHttpConfiguration {
    @Bean
    fun connectionPool() = ConnectionPool()

    @Bean
    fun okHttp3Dispatcher() = Dispatcher().apply {
        maxRequestsPerHost = Int.MAX_VALUE
    }
}