package ru.spliterash.vkVideoUnlocker.video.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity
import ru.spliterash.vkVideoUnlocker.video.exceptions.*
import ru.spliterash.vkVideoUnlocker.video.impl.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.repository.VideoRepository
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.time.Duration
import java.util.*

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
        .build<String, Deferred<FullVideo>> { key ->
            CoroutineScope(Dispatchers.IO).async {
                getVideoWithTryingLockBehavior(
                    key
                )
            }
        }
    private val unlocksInProgress = Collections.synchronizedMap(hashMapOf<String, Deferred<String>>())
    private val ignoreExceptionScope = CoroutineScope(
        SupervisorJob()
    )

    fun baseCheckVideo(video: VkVideo) {
        if (video.isPrivate)
            throw VideoPrivateException()
        if (video.platform != null)
            throw VideoFromAnotherPlatformException()
        if (video.ownerId == -groupUser.id)
            throw SelfVideoException()
    }

    fun wrap(videoId: String): VideoHolder {
        return StringHolder(videoId)
    }

    fun wrap(video: VkVideo): VideoHolder {
        return InfoVideoHolder(video)
    }


    suspend fun getUnlockedId(holder: VideoHolder): String {
        val originalVideoId = holder.id
        return unlocksInProgress.computeIfAbsent(originalVideoId) {
            ignoreExceptionScope.async {
                try {
                    _getUnlockedId(holder)
                } finally {
                    @Suppress("DeferredResultUnused")
                    unlocksInProgress.remove(originalVideoId)
                }
            }
        }.await()
    }

    private suspend fun _getUnlockedId(holder: VideoHolder): String {
        val originalVideoId = holder.id
        val unlocked = videoRepository.findVideo(originalVideoId)
        if (unlocked != null)
            return unlocked.unlockedId
        val video = holder.video()
        baseCheckVideo(video)

        val locked = isLocked(originalVideoId)
        if (!locked)
            throw VideoOpenException()
        val availableVideo = holder.fullVideo()

        return reUploadAndSave(availableVideo)
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

    suspend fun getInfoForDownload(videoId: String): FullVideo {
        return downloadCache.get(videoId).await()
    }

    /**
     * Я не умею называть методы. Попробовать получить видео, и в случае если оно недоступно, вступить в группу
     */
    suspend fun getVideoWithTryingLockBehavior(videoId: String): FullVideo {
        // Прежде всего попробуем просто его получить
        try {
            val video = workUser.videos.getVideo(videoId)
            return FullVideo(
                video,
                null,
                videoAccessorFactory,
                workUserGroupService,
            )
        } catch (_: VideoLockedException) {
        }

        val split = videoId.split("_")
        val ownerId = split[0].toInt()
        if (ownerId > 0)
            throw WeDoNotWorkWithLockedUserVideosException()

        val groupId = -ownerId
        val status = workUserGroupService.joinGroup(groupId)

        return FullVideo(workUser.videos.getVideo(videoId), status, videoAccessorFactory, workUserGroupService)
    }

    suspend fun isLocked(videoId: String): Boolean {
        return try {
            pokeUser.videos.getVideo(videoId)
            false
        } catch (ex: VideoLockedException) {
            true
        }
    }

    private inner class StringHolder(override val id: String) : VideoHolder {
        private lateinit var full: FullVideo
        private suspend fun check() {
            if (!this::full.isInitialized)
                full = getVideoWithTryingLockBehavior(id)
        }

        override suspend fun video(): VkVideo {
            check()
            return full.video
        }

        override suspend fun fullVideo(): FullVideo {
            check()
            return full
        }
    }

    private inner class InfoVideoHolder(val video: VkVideo) : VideoHolder {
        private lateinit var full: FullVideo
        override val id: String
            get() = video.normalId()

        override suspend fun video() = video

        override suspend fun fullVideo(): FullVideo {
            if (!this::full.isInitialized)
                full = getVideoWithTryingLockBehavior(id)

            return full
        }
    }
}