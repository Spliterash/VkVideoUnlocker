package ru.spliterash.vkVideoUnlocker.video.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.CoroutineHelper.scope
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Keyboard
import ru.spliterash.vkVideoUnlocker.messageChain.handlers.SaveVideoChain.Payload
import ru.spliterash.vkVideoUnlocker.video.api.VideosCommons
import ru.spliterash.vkVideoUnlocker.video.controller.request.VideoSaveRequest
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoSaveExpireException
import ru.spliterash.vkVideoUnlocker.video.service.dto.VideoSaveEntry
import java.util.*
import java.util.concurrent.TimeUnit

@Singleton
class VideoSaveService(
    private val commons: VideosCommons,
    @Value("\${vk-unlocker.miniAppId}")
    private val appId: String,
    private val mapper: ObjectMapper,
) {
    private val pending = Caffeine
        .newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build<UUID, VideoSaveEntry>()
        .asMap()

    // Вот это лесенка, прикольна
    fun createKeyboard(pendingId: UUID, full: FullVideo): Keyboard {
        return Keyboard(
            listOf(
                listOf(
                    Keyboard.Button(
                        Keyboard.Button.OpenAppAction(
                            appId,
                            "Открыть сохранялку",
                            mapper.writeValueAsString(
                                Payload(
                                    pendingId.toString(),
                                    Payload.Video(
                                        full.originalAttachmentId,
                                        full.video.title,
                                        full.video.preview().toString()
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    fun createPendingVideo(
        videoSource: InputStreamSource,
        message: RootMessage,
        editable: EditableMessage,
    ): VideoSaveEntry {
        val id = UUID.randomUUID()
        val entry = VideoSaveEntry(
            id = id,
            userId = message.fromId,
            message = editable,
            accessor = videoSource
        )
        pending[id] = entry

        return entry
    }

    suspend fun processUrl(input: VideoSaveRequest) {
        val (id, userId, groupId, uploadUrl) = input
        val entry = pending.remove(id) ?: throw VideoSaveExpireException()
        scope.launch {
            val editMessageTask = launch {
                delay(500) // мб оно оч быстро всё перезальёт, а пользователь ещё miniapp не закрыл
                entry.message.sendOrUpdate("В процессе")
            }
            try {
                val savedId = commons.upload(uploadUrl, entry.accessor)
                val ownerId = if (groupId == null) userId else -groupId
                entry.message.sendOrUpdate(
                    "Успешно.\n" +
                            "Если назначение является закрытой группой или страницей, то видео не будет прикреплено к сообщению, но, оно есть там, куда ты сохранил",
                    "video${ownerId}_${savedId}"
                )
            } catch (ex: Exception) {
                entry.message.sendOrUpdate("Ошибка при загрузке(${ex.javaClass.simpleName}): ${ex.localizedMessage}")
            } finally {
                editMessageTask.cancel()
            }
        }
    }
}