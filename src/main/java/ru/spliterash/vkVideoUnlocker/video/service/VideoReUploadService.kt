package ru.spliterash.vkVideoUnlocker.video.service

import jakarta.inject.Singleton
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity
import ru.spliterash.vkVideoUnlocker.video.exceptions.SelfVideoException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoOpenException
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.repository.VideoRepository
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.*

@Singleton
class VideoReUploadService(
    private val videoService: VideoService,
    private val videoRepository: VideoRepository,
    @WorkUser private val workUser: VkApi,
    @GroupUser private val groupUser: VkApi,
) {
    private val inProgress = Collections.synchronizedMap(hashMapOf<String, Deferred<String>>())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun getUnlockedId(holder: VideoHolder): String {
        return inProgress.computeIfAbsent(holder.id) {
            scope.async {
                try {
                    actualGetUnlockedId(holder)
                } finally {
                    inProgress.remove(holder.id)
                }
            }
        }.await()
    }

    private suspend fun actualGetUnlockedId(holder: VideoHolder): String {
        val originalVideoId = holder.id
        val unlocked = videoRepository.findVideo(originalVideoId)
        if (unlocked != null)
            return unlocked.unlockedId
        val video = holder.video()
        checkForReUpload(video)

        // Проверяем на закрытость только видео
        if (holder.type == VideoHolder.VideoHolderType.VIDEO) {
            val locked = videoService.isLocked(originalVideoId)
            if (!locked)
                throw VideoOpenException()
        }

        val availableVideo = holder.fullVideo()

        return reUploadAndSave(availableVideo)
    }

    private fun checkForReUpload(video: VkVideo) {
        if (video.ownerId == -groupUser.id)
            throw SelfVideoException()
    }

    private suspend fun reUploadAndSave(fullVideo: FullVideo): String {
        val originalVideoId = fullVideo.video.normalId()
        val videoAccessor = fullVideo.toAccessor()
        val groupStatus = fullVideo.status()


        val reUploadedId = workUser.videos.upload(
            groupUser.id, originalVideoId,
            groupStatus != GroupStatus.PUBLIC,
            videoAccessor
        )

        val entity = VideoEntity(originalVideoId, reUploadedId)
        videoRepository.save(entity)

        return reUploadedId
    }
}