package ru.spliterash.vkVideoUnlocker.video.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoLockedException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoNotFoundException
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkSaveResponse
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideoUploadResponse
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.readResponse
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkItemsResponse

@Prototype
class VideosImpl(
    @Parameter private val client: OkHttpClient,
    private val mapper: ObjectMapper,
    private val helper: VkHelper
) : Videos {

    override suspend fun getVideo(id: String): VkVideo {
        val mapped = VkConst.requestBuilder()
            .get()
            .addHeader(
                "user-agent",
                "KateMobileAndroid/99 lite-535 (Android 11; SDK 30; arm64-v8a; asus Zenfone Max Pro M1; ru)"
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
            .readResponse(helper, VkSaveResponse::class.java)
            .uploadUrl

        val info = accessor.load()

        val response = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "multipart/form-data")
            .post(
                MultipartBody.Builder()
                    .addFormDataPart("video_file", "video.mp4", object : RequestBody() {
                        override fun contentType(): MediaType {
                            return "application/octet-stream".toMediaType()
                        }

                        override fun writeTo(sink: BufferedSink) {
                            info.stream.use { `in` ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var completed = 0L
                                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                                    sink.write(buffer, 0, bytesRead)
                                    completed += bytesRead

                                    progressMeter.onProgress(completed, info.contentLength)
                                }
                            }
                        }

                        override fun contentLength(): Long {
                            return info.contentLength
                        }

                        override fun isOneShot(): Boolean {
                            return true
                        }
                    })
                    .build()
            )
            .build()
            .executeAsync(client)

        val raw = response.body.string()
        val mapped = mapper.readValue<VkVideoUploadResponse>(raw)

        return "-${groupId}_${mapped.videoId}"
    }

}