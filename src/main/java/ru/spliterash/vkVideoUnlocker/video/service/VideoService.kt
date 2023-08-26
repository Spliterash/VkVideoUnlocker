package ru.spliterash.vkVideoUnlocker.video.service

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.story.exceptions.CantSeeStoryException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryExpiredException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryNotVideoException
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.exceptions.*
import ru.spliterash.vkVideoUnlocker.video.holder.StoryHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.normalId
import ru.spliterash.vkVideoUnlocker.vk.AttachmentScanner
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class VideoService(
    private val attachmentScanner: AttachmentScanner,
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

    fun wrapVideoId(videoId: String): VideoContentHolder {
        return StringVideoHolder(videoId)
    }

    fun wrapStoryId(storyId: String): VideoContentHolder {
        return StringStoryHolder(storyId)
    }

    suspend fun wrapAttachmentId(attachmentId: String): VideoContentHolder? {
        val matcher = VkConst.VK_ATTACHMENT_PATTERN.matcher(attachmentId)
        if (!matcher.find())
            throw IllegalArgumentException("Attachment id has wrong format")

        val type = matcher.group("type")
        val ownerId = matcher.group("owner")
        val id = matcher.group("id")
        val normalId = "${ownerId}_${id}"

        return when (type) {
            "video" -> wrapVideoId(normalId)
            "story" -> wrapStoryId(normalId)
            "wall" -> wrapWallId(normalId)
            else -> null
        }
    }

    suspend fun wrapWallId(wallId: String): VideoContentHolder? {
        val wall = workUser.walls.getById(wallId)
        val video = attachmentScanner.scanForAttachment(wall) { it.video }
        return if (video != null)
            return FullVideoHolder(video) // Пользователь получает полное видео если запросил стену
        else
            null
    }

    fun wrap(video: VkVideo): VideoContentHolder {
        baseCheckVideo(video)
        return InfoVideoHolder(video)
    }

    fun wrap(story: VkStory): VideoContentHolder {
        if (story.isExpired)
            throw StoryExpiredException()

        return if (story.canSee!!)
            FullStoryHolder(story)
        else
            wrapStoryId(story.normalId())
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

    // TODO, Вынести все холдеры в отдельные классы, наверное
    private inner class StringVideoHolder(
        override val videoId: String
    ) : VideoHolder {
        private lateinit var full: FullVideo
        override val attachmentId: String
            get() = "video$videoId"

        private suspend fun check() {
            if (!this::full.isInitialized) {
                full = getVideoWithTryingLockBehavior(videoId)
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
    }

    private inner class FullVideoHolder(val video: VkVideo) : VideoHolder {
        override val videoId: String
            get() = video.normalId()
        override val attachmentId: String
            get() = "video$videoId"

        override suspend fun video(): VkVideo {
            return video
        }

        override suspend fun fullVideo(): FullVideo {
            return FullVideo(
                video,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }

    }

    private inner class InfoVideoHolder(val video: VkVideo) : VideoHolder {
        private lateinit var full: FullVideo
        override val videoId: String
            get() = video.normalId()
        override val attachmentId: String
            get() = "video${video.normalId()}"

        override suspend fun video() = video

        override suspend fun fullVideo(): FullVideo {
            if (!this::full.isInitialized) {
                full = getVideoWithTryingLockBehavior(video.normalId())
                baseCheckVideo(full.video)
            }

            return full
        }
    }

    private fun baseCheckStory(story: VkStory) {
        if (!story.canSee!!)
            throw CantSeeStoryException()
        if (story.type != VkStory.Type.VIDEO)
            throw StoryNotVideoException()
    }

    private suspend fun findStoryById(id: String): VkStory {
        return workUser.stories.getById(id)
    }

    private inner class StringStoryHolder(
        override val storyId: String
    ) : StoryHolder {
        override val attachmentId: String
            get() = "story$storyId"
        private var fullStory: VkStory? = null

        private suspend fun fullStory(): VkStory {
            fullStory?.let {
                baseCheckStory(it)
                return it
            }
            return findStoryById(storyId).also {
                fullStory = it
                baseCheckStory(it)
            }
        }

        override suspend fun video(): VkVideo {
            return fullStory().video!!
        }

        override suspend fun fullVideo(): FullVideo {
            return FullVideo(
                video(),
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }

    }

    private inner class FullStoryHolder(
        val story: VkStory
    ) : StoryHolder {
        override val storyId: String
            get() = story.normalId()
        override val attachmentId: String
            get() = "story${story.normalId()}"

        override suspend fun video(): VkVideo {
            return story.video!!
        }

        override suspend fun fullVideo(): FullVideo {
            return FullVideo(
                story.video!!,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }
    }
}