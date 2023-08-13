package ru.spliterash.vkVideoUnlocker.video.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoFromAnotherPlatformException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoLockedException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoPrivateException
import ru.spliterash.vkVideoUnlocker.video.exceptions.WeDoNotWorkWithLockedUserVideosException
import ru.spliterash.vkVideoUnlocker.video.impl.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.repository.VideoRepository
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.time.Duration

@Singleton
class VideoService(
    private val workUserGroupService: WorkUserGroupService,
    private val videoAccessorFactory: VideoAccessorFactory,
    private val videoRepository: VideoRepository,
    @GroupUser private val groupUser: VkApi,
    @WorkUser private val workUser: VkApi,
    @PokeUser private val pokeUser: VkApi,
) {
    private val downloadCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(30))
        .build<String, Deferred<VideoDownloadInfo>> { key ->
            CoroutineScope(Dispatchers.IO).async {
                _getInfoForDownload(
                    key
                )
            }
        }

    fun baseCheckVideo(video: VkVideo) {
        if (video.isPrivate)
            throw VideoPrivateException()
        if (video.platform != null)
            throw VideoFromAnotherPlatformException()
    }

    /**
     * Получить ID разблокированного видоса
     */
    suspend fun getUnlockedId(video: VkVideo): String {
        val originalVideoId = video.normalId()
        val unlocked = videoRepository.findVideo(originalVideoId)
        if (unlocked != null)
            return unlocked.unlockedId

        val reUploadedId = reUpload(originalVideoId)

        val entity = VideoEntity(originalVideoId, reUploadedId)
        videoRepository.save(entity)

        return reUploadedId
    }

    /**
     * Перезалить видео в группу
     */
    private suspend fun reUpload(originalVideoId: String): String {
        val info = _getInfoForDownload(originalVideoId)

        return workUser.videos.upload(
            groupUser.id, originalVideoId,
            info.groupStatus != GroupStatus.PUBLIC,
            info.accessor
        )
    }

    /**
     * Получить объект для скачивания видео
     */
    private suspend fun _getInfoForDownload(videoId: String): VideoDownloadInfo {
        // Прежде всего попробуем просто его получить
        try {
            val video = workUser.videos.getVideo(videoId)
            return VideoDownloadInfo(
                videoAccessorFactory.create(video),
                GroupStatus.PUBLIC
            ) // Если мы так просто получили линк, значит видос открытый
        } catch (_: VideoLockedException) {
        }

        val split = videoId.split("_")
        val ownerId = split[0].toInt()
        if (ownerId > 0)
            throw WeDoNotWorkWithLockedUserVideosException()

        val groupId = -ownerId
        val status = workUserGroupService.joinGroup(groupId)
        val video = workUser.videos.getVideo(videoId)

        return VideoDownloadInfo(videoAccessorFactory.create(video), status)
    }

    suspend fun getInfoForDownload(videoId: String): VideoDownloadInfo {
        return downloadCache.get(videoId).await()
    }

    suspend fun isLocked(videoId: String): Boolean {
        return try {
            pokeUser.videos.getVideo(videoId)
            false
        } catch (ex: VideoLockedException) {
            true
        }
    }
}