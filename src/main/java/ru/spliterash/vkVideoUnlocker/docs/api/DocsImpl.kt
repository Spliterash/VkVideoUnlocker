package ru.spliterash.vkVideoUnlocker.docs.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.FormBody
import okhttp3.OkHttpClient
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.common.VkUploaderService
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.common.vkModels.VkUploadUrlResponse
import ru.spliterash.vkVideoUnlocker.docs.vkModels.VkFileSaveResponse
import ru.spliterash.vkVideoUnlocker.docs.vkModels.VkFileUploadResponse
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.VkHelper
import ru.spliterash.vkVideoUnlocker.vk.readResponse

@Prototype
class DocsImpl(
    @Parameter private val client: OkHttpClient,
    private val helper: VkHelper,
    private val vkUploaderService: VkUploaderService,
    private val objectMapper: ObjectMapper,
) : Docs {
    override suspend fun uploadAudioMessage(peerId: Long, info: InputStreamSource.Info): String {
        val uploadUrl = VkConst.requestBuilder()
            .get()
            .url(
                VkConst.urlBuilder("docs.getMessagesUploadServer")
                    .addQueryParameter("peer_id", peerId.toString())
                    .addQueryParameter("type", "audio_message")
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, VkUploadUrlResponse::class.java)
            .uploadUrl

        val response = vkUploaderService.upload(uploadUrl, null, "file", "voice.opus", info)
        val mapped = objectMapper.readValue<VkFileUploadResponse>(response)

        val saveResponse = VkConst.requestBuilder()
            .url(VkConst.urlBuilder("docs.save").build())
            .post(
                FormBody.Builder()
                    .add("file", mapped.file)
                    .build()
            )
            .build()
            .executeAsync(client)
            .readResponse(helper, VkFileSaveResponse::class.java)

        val key = listOfNotNull(
            saveResponse.audioMessage.ownerId,
            saveResponse.audioMessage.id,
            saveResponse.audioMessage.accessKey
        )
            .joinToString("_")

        return "${saveResponse.type}$key"
    }
}