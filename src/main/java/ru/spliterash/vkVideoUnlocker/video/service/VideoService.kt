package ru.spliterash.vkVideoUnlocker.video.service

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.story.exceptions.CantSeeStoryException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryExpiredException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryNotVideoException
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.exceptions.*
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class VideoService(
    private val workUserGroupService: WorkUserGroupService,
    private val videoAccessorFactory: VideoAccessorFactory,
    @WorkUser private val workUser: VkApi,
    @PokeUser private val pokeUser: VkApi,
) {
    /**
     * Базовые проверки видосов
     */
    private fun baseCheckVideo(video: VkVideo) {
        if (video.isPrivate)
            throw VideoPrivateException()
        if (video.platform != null)
            throw VideoFromAnotherPlatformException()
        if (video.duration > 60 * 5)
            throw VideoTooLongException()
    }

    fun wrap(videoId: String, root: RootMessage): VideoHolder {
        return StringVideoHolder(videoId, root)
    }

    fun wrap(video: VkVideo, root: RootMessage): VideoHolder {
        baseCheckVideo(video)
        return InfoVideoHolder(video, root)
    }

    fun wrap(story: VkStory, root: RootMessage): VideoHolder {
        if (story.isExpired)
            throw StoryExpiredException()
        if (story.type != VkStory.Type.VIDEO)
            throw StoryNotVideoException()

        if (!story.canSee!!)
            throw CantSeeStoryException()

        return StoryHolder(story, root)
    }

    /**
     * Я не умею называть методы. Попробовать получить видео, и в случае если оно недоступно, вступить в группу
     *
     * Необходимо проверить видео через baseCheck перед использованием
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

    private inner class StringVideoHolder(
        override val id: String,
        override val root: RootMessage
    ) : VideoHolder {
        private lateinit var full: FullVideo
        private suspend fun check() {
            if (!this::full.isInitialized) {
                full = getVideoWithTryingLockBehavior(id)
                baseCheckVideo(full.video)
            }
        }

        override suspend fun video(): VkVideo {
            check()
            return full.video
        }

        override suspend fun fullVideo(): FullVideo {
            check()
            return full
        }

        override val type: VideoHolder.VideoHolderType
            get() = VideoHolder.VideoHolderType.VIDEO
    }

    private inner class InfoVideoHolder(val video: VkVideo, override val root: RootMessage) : VideoHolder {
        private lateinit var full: FullVideo
        override val id: String
            get() = video.normalId()

        override suspend fun video() = video

        override suspend fun fullVideo(): FullVideo {
            if (!this::full.isInitialized) {
                full = getVideoWithTryingLockBehavior(id)
                baseCheckVideo(full.video)
            }

            return full
        }

        override val type: VideoHolder.VideoHolderType
            get() = VideoHolder.VideoHolderType.VIDEO
    }

    private inner class StoryHolder(
        val story: VkStory,
        override val root: RootMessage
    ) : VideoHolder {
        override val id: String
            get() = story.video!!.normalId()

        override suspend fun video() = story.video!!

        // LongPoll присылает нам сторисы с полным видео
        override suspend fun fullVideo(): FullVideo {
            return FullVideo(
                story.video!!,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }

        override val type: VideoHolder.VideoHolderType
            get() = VideoHolder.VideoHolderType.STORY
    }
}