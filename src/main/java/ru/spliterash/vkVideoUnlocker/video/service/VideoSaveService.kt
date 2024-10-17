package ru.spliterash.vkVideoUnlocker.video.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.CoroutineHelper.scope
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.video.api.VideosCommons
import ru.spliterash.vkVideoUnlocker.video.controller.request.VideoSaveRequest
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoSaveExpireException
import ru.spliterash.vkVideoUnlocker.video.service.dto.VideoSaveEntry
import java.util.*
import java.util.concurrent.TimeUnit

@Singleton
class VideoSaveService(
    private val commons: VideosCommons,
) {
    private val pending = Caffeine
        .newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build<UUID, VideoSaveEntry>()
        .asMap()


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
                            "По какой то непонятной мне причине, видео может быть не прикреплено к сообщению, в таком случае просто имей ввиду, что оно сохранилось туда, куда ты указал",
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