package ru.spliterash.vkVideoUnlocker.video

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import java.net.URL

@Singleton
class DownloadUrlSupplier(
    @Value("\${vk-unlocker.domain}") private val domain: String,
) {
    fun downloadUrl(attachmentId: String): URL {
        return URL(domain + Routes.DOWNLOAD.replace("{attachmentId}", attachmentId) + ".mp4")
    }
}