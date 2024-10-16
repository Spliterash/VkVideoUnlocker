package ru.spliterash.vkVideoUnlocker.video.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.common.VkUploaderService
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideoUploadResponse

@Singleton
class VideosCommons(
    private val vkUploaderHelper: VkUploaderService,
    private val mapper: ObjectMapper,
) {
    suspend fun upload(
        url: String,
        accessor: InputStreamSource,
        progressMeter: ProgressMeter? = null
    ): String {

        val info = accessor.load()

        val response = vkUploaderHelper.upload(url, progressMeter, "video", "video.mp4", info)
        val mapped = mapper.readValue<VkVideoUploadResponse>(response)

        return mapped.videoId
    }
}