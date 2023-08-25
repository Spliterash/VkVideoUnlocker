package ru.spliterash.vkVideoUnlocker.video.service

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryExpiredException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryIsPrivateException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryNotVideoException
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
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
        if (video.duration > 60 * 5)
            throw VideoTooLongException()
    }

    fun wrap(videoId: String, root: RootMessage): VideoHolder {
        return StringHolder(videoId, root)
    }

    fun wrap(video: VkVideo, root: RootMessage): VideoHolder {
        return InfoVideoHolder(video, root)
    }

    fun wrap(story: VkStory, root: RootMessage): VideoHolder {
        if (story.isExpired)
            throw StoryExpiredException()
        if (!story.canSee!!)
            throw StoryIsPrivateException()
        if (story.type != VkStory.Type.VIDEO)
            throw StoryNotVideoException()

        return StoryHolder(story, root)
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

        if (holder.isForce()) {
            val locked = isLocked(originalVideoId)
            if (!locked)
                throw VideoOpenException()
        }

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

        val video = try {
            workUser.videos.getVideo(videoId)
        } catch (locked: VideoLockedException) {
            // Вк по какой то непонятной причине говорит что видео доступно только подписчикам, если оно приватное
            throw VideoPrivateException()
        }
        return FullVideo(video, status, videoAccessorFactory, workUserGroupService)
    }

    suspend fun isLocked(videoId: String): Boolean {
        return try {
            pokeUser.videos.getVideo(videoId)
            false
        } catch (ex: VideoLockedException) {
            true
        }
    }

    private inner class StringHolder(
        override val id: String,
        override val root: RootMessage
    ) : VideoHolder {
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

    private inner class InfoVideoHolder(val video: VkVideo, override val root: RootMessage) : VideoHolder {
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

    private inner class StoryHolder(
        val story: VkStory,
        override val root: RootMessage
    ) :
        VideoHolder {
        override val id: String
            get() = story.video!!.normalId()

        override suspend fun video() = story.video!!

        // LongPoll присылает нам сторисы с полным видео
        override suspend fun fullVideo() = FullVideo(
            story.video!!,
            null,
            videoAccessorFactory,
            workUserGroupService
        )

        override fun isForce() = true
    }
}