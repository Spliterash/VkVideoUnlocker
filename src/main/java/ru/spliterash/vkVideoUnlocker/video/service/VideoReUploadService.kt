package ru.spliterash.vkVideoUnlocker.video.service

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import ru.spliterash.vkVideoUnlocker.common.CoroutineHelper
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity
import ru.spliterash.vkVideoUnlocker.video.exceptions.PrivateVideoDisabledException
import ru.spliterash.vkVideoUnlocker.video.exceptions.SelfVideoException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoOpenException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoTooLongException
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.repository.VideoRepository
import ru.spliterash.vkVideoUnlocker.video.service.dto.UnlockResult
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.UploadUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.*

@Singleton
class VideoReUploadService(
    private val videoRepository: VideoRepository,
    @UploadUser private val uploadUser: VkApi,
    @GroupUser private val groupUser: VkApi,
    @Value("\${vk-unlocker.private-groups:false}") private val privateGroupWork: Boolean
) {
    private val inProgress = Collections.synchronizedMap(hashMapOf<String, Deferred<UnlockResult>>())
    private val scope = CoroutineHelper.scope
    suspend fun getUnlockedId(holder: VideoContentHolder): UnlockResult {
        return inProgress.computeIfAbsent(holder.attachmentId) {
            scope.async {
                try {
                    actualGetUnlockedId(holder)
                } finally {
                    inProgress.remove(holder.attachmentId)
                }
            }
        }.await()
    }

    private suspend fun softGetUnlockedId(holder: VideoContentHolder): UnlockResult? {
        val attachmentId = holder.attachmentId
        val unlocked = videoRepository.findVideo(attachmentId)
        if (unlocked != null)
            return UnlockResult(unlocked.unlockedId, unlocked.private)
        val video = holder.video()
        checkForReUpload(video)

        // Проверяем на закрытость только видео
        if (holder is VideoHolder) {
            val locked = holder.isLocked()
            if (!locked)
                throw VideoOpenException()
        }
        return null
    }

    private suspend fun actualGetUnlockedId(holder: VideoContentHolder): UnlockResult {
        val unlockedId = softGetUnlockedId(holder)
        if (unlockedId != null) return unlockedId

        return reUploadAndSave(holder)
    }

    private fun checkForReUpload(video: VkVideo) {
        if (video.ownerId == -groupUser.id)
            throw SelfVideoException()
    }

    private suspend fun reUploadAndSave(holder: VideoContentHolder): UnlockResult {
        val fullVideo = holder.fullVideo()

        val originalAttachmentId = holder.attachmentId
        val private = fullVideo.shouldBeLocked(false)
        if (private && !privateGroupWork) throw PrivateVideoDisabledException()
        if (fullVideo.video.duration > 60 * 5) throw VideoTooLongException()

        val videoAccessor = fullVideo.toAccessor()

        val reUploadedId = uploadUser.videos.upload(
            groupUser.id,
            originalAttachmentId,
            private,
            videoAccessor
        )

        val entity = VideoEntity(originalAttachmentId, reUploadedId, private)
        videoRepository.save(entity)

        return UnlockResult(reUploadedId, private)
    }
}