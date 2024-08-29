package ru.spliterash.vkVideoUnlocker.photo.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.FormBody
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.common.VkUploaderService
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.common.vkModels.VkSaveResponse
import ru.spliterash.vkVideoUnlocker.common.vkModels.VkUploadUrlResponse
import ru.spliterash.vkVideoUnlocker.photo.vkModels.PhotoUploadResponse
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.readResponse

@Prototype
class PhotosImpl(
    @Parameter private val client: OkHttpClient,
    private val vkUploaderService: VkUploaderService,
    private val helper: VkHelper,
    private val objectMapper: ObjectMapper,
) : Photos {
    override suspend fun uploadPhoto(peerId: Long, source: InputStreamSource): String {
        val info = source.load()
        val uploadUrl = VkConst.requestBuilder()
            .get()
            .url(
                VkConst.urlBuilder("photos.getMessagesUploadServer")
                    .addQueryParameter("peer_id", peerId.toString())
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, VkUploadUrlResponse::class.java)
            .uploadUrl

        val response = vkUploaderService.upload(uploadUrl, null, "photo", "image.jpg", info)
        val mapped = objectMapper.readValue<PhotoUploadResponse>(response)

        if (mapped.photo.isEmpty() || mapped.photo == "[]") throw IllegalStateException("Photo upload has empty photo field")

        val saveResponse = VkConst.requestBuilder()
            .url(VkConst.urlBuilder("photos.saveMessagesPhoto").build())
            .post(
                FormBody.Builder()
                    .add("photo", mapped.photo)
                    .add("server", mapped.server)
                    .add("hash", mapped.hash)
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, VkSaveResponse::class.java)

        val key = listOfNotNull(saveResponse.ownerId, saveResponse.id, saveResponse.accessKey)
            .joinToString("_")

        return "photo$key"
    }
}