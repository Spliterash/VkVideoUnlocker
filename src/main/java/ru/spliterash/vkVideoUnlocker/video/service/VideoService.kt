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
import ru.spliterash.vkVideoUnlocker.video.holder.AbstractVideoContentHolder
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

    fun wrapVideoId(videoId: String): VideoHolder {
        return StringVideoHolder(videoId)
    }

    fun wrapStoryId(storyId: String): StoryHolder {
        return StringStoryHolder(storyId)
    }

    suspend fun wrapAttachmentId(attachmentId: String): VideoContentHolder {
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
            else -> throw IllegalArgumentException("Unsupported attachment type")
        }
    }

    suspend fun wrapWallId(wallId: String): VideoContentHolder {
        val wall = workUser.walls.getById(wallId)
        val video = attachmentScanner.scanForAttachment(wall) { it.video }
        if (video != null)
            return FullVideoHolder(video) // Пользователь получает полное видео если запросил стену
        else
            throw VideoNotFoundException()
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
    private suspend fun getVideoWithTryingLockBehavior(holder: VideoLoader): FullVideo {
        // Прежде всего попробуем просто его получить
        try {
            val video = holder.loadVideo()
            return FullVideo(
                video,
                null,
                videoAccessorFactory,
                workUserGroupService,
            )
        } catch (_: VideoLockedException) {
        } catch (_: CantSeeStoryException) {
        }

        val ownerId = holder.ownerId
        if (ownerId > 0)
            throw WeDoNotWorkWithLockedUserVideosException()

        val groupId = -ownerId
        val status = workUserGroupService.joinGroup(groupId)

        val video = try {
            holder.loadVideo()
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

    private interface VideoLoader {
        val ownerId: Int
        suspend fun loadVideo(): VkVideo
    }

    private abstract inner class AbstractVideoHolder(contentId: String) : AbstractVideoContentHolder(contentId),
        VideoHolder {
        override val attachmentId: String
            get() = "video$contentId"

        override suspend fun isLocked(): Boolean {
            return isLocked(contentId)
        }
    }

    // TODO, Вынести все холдеры в отдельные классы, наверное
    private inner class StringVideoHolder(contentId: String) : AbstractVideoHolder(contentId), VideoLoader {

        override suspend fun video(): VkVideo {
            return fullVideo().video
        }

        override suspend fun loadFullVideo(): FullVideo {
            val full = getVideoWithTryingLockBehavior(this)
            baseCheckVideo(full.video)

            return full
        }

        override suspend fun loadVideo() = workUser.videos.getVideo(contentId)
    }

    private inner class FullVideoHolder(val video: VkVideo) : AbstractVideoHolder(video.normalId()) {

        override suspend fun video(): VkVideo {
            return video
        }

        override suspend fun loadFullVideo(): FullVideo {
            return FullVideo(
                video,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }
    }

    private inner class InfoVideoHolder(val video: VkVideo) : AbstractVideoHolder(video.normalId()), VideoLoader {
        override suspend fun video() = video
        override suspend fun loadFullVideo(): FullVideo {
            val full = getVideoWithTryingLockBehavior(this)
            baseCheckVideo(full.video)

            return full
        }

        override suspend fun loadVideo() = workUser.videos.getVideo(contentId)
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

    private abstract inner class AbstractStoryHolder(contentId: String) :
        AbstractVideoContentHolder(contentId),
        StoryHolder {
        override val attachmentId: String
            get() = "story$contentId"
    }

    private inner class StringStoryHolder(contentId: String) : AbstractStoryHolder(contentId), VideoLoader {
        override val attachmentId: String
            get() = "story$contentId"

        override suspend fun video(): VkVideo {
            return fullVideo().video
        }

        override suspend fun loadFullVideo(): FullVideo {
            return getVideoWithTryingLockBehavior(this)
        }

        override suspend fun loadVideo(): VkVideo {
            val story = findStoryById(contentId)
            baseCheckStory(story)

            return story.video!!
        }
    }

    private inner class FullStoryHolder(
        val story: VkStory
    ) : AbstractStoryHolder(story.normalId()) {

        override suspend fun video(): VkVideo {
            return story.video!!
        }

        override suspend fun loadFullVideo(): FullVideo {
            baseCheckStory(story)
            return FullVideo(
                story.video!!,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }
    }
}