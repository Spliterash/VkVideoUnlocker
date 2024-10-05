package ru.spliterash.vkVideoUnlocker.video.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.OkHttpClient
import org.apache.commons.io.IOUtils
import ru.spliterash.vkVideoUnlocker.common.VkUploaderService
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.common.vkModels.VkUploadUrlResponse
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoLockedException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoNotFoundException
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideoUploadResponse
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.readResponse
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkItemsResponse
import java.io.FileOutputStream

@Prototype
class VideosImpl(
    @Parameter private val client: OkHttpClient,
    private val vkUploaderHelper: VkUploaderService,
    private val mapper: ObjectMapper,
    private val helper: VkHelper
) : Videos {

    override suspend fun getVideo(id: String): VkVideo {
        val mapped = VkConst.requestBuilder()
            .get()
            .addHeader(
                "user-agent",
                USER_AGENT
            )
            .url(
                VkConst.urlBuilder("video.get")
                    .addQueryParameter("extended", "1")
                    .addQueryParameter("videos", id)
                    .addQueryParameter("fields", "privacy_view")
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, object : TypeReference<VkItemsResponse<VkVideo>>() {})

        val video = mapped.items.firstOrNull() ?: throw VideoNotFoundException()
        if (video.contentRestricted)
            throw VideoLockedException()

        return video
    }

    override suspend fun upload(
        groupId: Int,
        name: String,
        private: Boolean,
        accessor: VideoAccessor,
        progressMeter: ProgressMeter
    ): String {
        val url = VkConst.requestBuilder()
            .get()
            .url(
                VkConst.urlBuilder("video.save")
                    .addQueryParameter("name", name)
                    .addQueryParameter("is_private", if (private) "1" else "0")
                    .addQueryParameter("group_id", groupId.toString())
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, VkUploadUrlResponse::class.java)
            .uploadUrl

        val info = accessor.load()

        val response = vkUploaderHelper.upload(url, progressMeter, "video", "video.mp4", info)
        val mapped = mapper.readValue<VkVideoUploadResponse>(response)

        return "-${groupId}_${mapped.videoId}"
    }

    companion object {
        const val USER_AGENT =
            "KateMobileAndroid/99 lite-535 (Android 11; SDK 30; arm64-v8a; asus Zenfone Max Pro M1; ru)"
    }
}