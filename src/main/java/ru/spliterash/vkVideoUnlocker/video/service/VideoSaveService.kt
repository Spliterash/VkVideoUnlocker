package ru.spliterash.vkVideoUnlocker.video.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.inject.Singleton
import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.CoroutineHelper.scope
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.video.api.VideosCommons
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

    suspend fun processUrl(id: UUID, uploadUrl: String) {
        val entry = pending.remove(id) ?: throw VideoSaveExpireException()
        scope.launch {
            try {
                val savedId = commons.upload(uploadUrl, entry.accessor)
                entry.message.sendOrUpdate("Успешно", "video${entry.userId}_$savedId")
            } catch (ex: Exception) {
                entry.message.sendOrUpdate("Ошибка при загрузке(${ex.javaClass.simpleName}): ${ex.localizedMessage}")
            }
        }
    }
}