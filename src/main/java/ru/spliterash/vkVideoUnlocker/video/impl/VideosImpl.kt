package ru.spliterash.vkVideoUnlocker.video.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import org.apache.commons.logging.LogFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.api.Videos
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoLockedException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoNotFoundException
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkSaveResponse
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideoGetResponse
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideoUploadResponse
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.vkModels.VkConst

private val log = LogFactory.getLog(VideosImpl::class.java)

@Prototype
class VideosImpl(
    @Parameter private val client: OkHttpClient,
    private val mapper: ObjectMapper,
    private val helper: VkHelper
) : Videos {

    override suspend fun getVideo(id: String): VkVideo {
        val request = VkConst.requestBuilder()
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

        val response = client
            .newCall(request)
            .executeAsync()

        val mapped = helper.readResponse(response, VkVideoGetResponse::class.java)
        val video = mapped.items.firstOrNull() ?: throw VideoNotFoundException()
        if (video.contentRestricted)
            throw VideoLockedException()

        return video
    }
    override suspend fun upload(groupId: Int, name: String, private: Boolean, accessor: VideoAccessor): String {
        val request = VkConst.requestBuilder()
            .get()
            .url(
                VkConst.urlBuilder("video.save")
                    .addQueryParameter("name", name)
                    .addQueryParameter("is_private", if (private) "1" else "0")
                    .addQueryParameter("group_id", groupId.toString())
                    .build()
            )
            .build()


        val uploadUrlResponse = client
            .newCall(request)
            .executeAsync()

        val url = helper.readResponse(uploadUrlResponse, VkSaveResponse::class.java).uploadUrl
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
                                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                                    sink.write(buffer, 0, bytesRead)
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