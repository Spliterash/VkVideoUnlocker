package ru.spliterash.vkVideoUnlocker.video.service

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.Wall
import ru.spliterash.vkVideoUnlocker.longpoll.message.isGroupChat
import ru.spliterash.vkVideoUnlocker.message.utils.MessageContentScanner
import ru.spliterash.vkVideoUnlocker.story.exceptions.CantSeeStoryException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryExpiredException
import ru.spliterash.vkVideoUnlocker.story.exceptions.StoryNotVideoException
import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.api.checkRestriction
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.exceptions.*
import ru.spliterash.vkVideoUnlocker.video.holder.AbstractVideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.holder.StoryHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.vkModels.publicId
import ru.spliterash.vkVideoUnlocker.vk.MessageScanner
import ru.spliterash.vkVideoUnlocker.vk.VkConst
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.DownloadUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class VideoService(
    private val messageScanner: MessageScanner,
    private val messageContentScanner: MessageContentScanner,
    private val workUserGroupService: WorkUserGroupService,
    private val videoAccessorFactory: VideoAccessorFactory,
    @GroupUser private val group: VkApi,
    @DownloadUser private val downloadUser: VkApi,
    @PokeUser private val pokeUser: VkApi,
) {
    /**
     * Базовые проверки видосов
     */
    private fun baseCheckVideo(video: VkVideo) {
        if (video.platform != null)
            throw VideoFromAnotherPlatformException()
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
        val key = matcher.group("key")

        var normalId = "${ownerId}_${id}"
        if (key != null) normalId += "_$key"

        return when (type) {
            "video" -> wrapVideoId(normalId)
            "story" -> wrapStoryId(normalId)
            "wall" -> wrapWallId(normalId)
            else -> throw IllegalArgumentException("Unsupported attachment type")
        }
    }

    suspend fun wrapWallId(wallId: String): VideoContentHolder {
        val wall = downloadUser.walls.getById(wallId)
        val video = messageScanner.scanForAttachment(wall) { it.video }
        if (video != null)
            return FullVideoHolder(video) // Пользователь получает полное видео если запросил стену
        else
            throw VideoNotFoundException()
    }

    fun wrap(video: VkVideo, chain: List<AttachmentContainer>): VideoContentHolder {
        baseCheckVideo(video)
        return InfoVideoHolder(video, chain)
    }

    fun wrap(story: VkStory): VideoContentHolder {
        if (story.isExpired)
            throw StoryExpiredException()

        // Vk moment. Какого то фига вк отдаёт кривые истории группе, которые нельзя скачать, так что перезапросим их от пользователя
        return/* if (story.canSee!!)
            GroupStoryHolder(story)
        else
           */ wrapStoryId(story.normalId())
    }

    /**
     * Я не умею называть методы. Попробовать получить видео, и в случае если оно недоступно, вступить в группу
     *
     * Необходимо проверить видео через baseCheck перед использованием
     */
    private suspend fun getVideoWithTryingLockBehavior(holder: VideoLoader): FullVideo {
        holder as VideoContentHolder // КРИНЖАТИНА!!!!

        // Прежде всего попробуем просто его получить
        try {
            val video = holder.loadVideo()
            return FullVideo(
                video,
                holder.attachmentId,
                null,
                videoAccessorFactory,
                workUserGroupService,
            )
        } catch (_: VideoLockedException) {
        } catch (_: CantSeeStoryException) {
        }


        val ownerId = holder.ownerId
        if (ownerId > 0) {
            val video = tryMessageGetBehavior(holder)

            return FullVideo(video, holder.attachmentId, null, videoAccessorFactory, workUserGroupService)
        }

        val groupId = -ownerId
        val status = workUserGroupService.joinGroup(groupId)

        val video = try {
            holder.loadVideo()
        } catch (locked: VideoLockedException) {
            tryMessageGetBehavior(holder)
        }
        return FullVideo(video, holder.attachmentId, status, videoAccessorFactory, workUserGroupService)
    }

    private suspend fun tryMessageGetBehavior(holder: VideoContentHolder): VkVideo {
        if (holder !is InfoVideoHolder) throw InfoVideoRequiredException()
        val chain = holder.source
        val source = chain.first() as RootMessage
        if (source.isGroupChat()) throw PersonalChatRequiredException()

        val rootMessageByUser = downloadUser.messages.messageById(messageId = source.id, groupId = group.id)
        val result = messageContentScanner.findContent(rootMessageByUser)
        if (result == null || result.content !is VkVideo || result.content.publicId() != holder.contentId) throw ContentNotFoundException()
        result.content.checkRestriction()

        return result.content
    }

    suspend fun isLocked(videoId: String): Boolean {
        return try {
            pokeUser.videos.getVideo(videoId)
            false
        } catch (ex: VideoLockedException) {
            true
        }
    }

    // Какой же кринж, я всё понимаю, но мне мега впадлу переделывать
    // Простите если вы это видите
    private interface VideoLoader {
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

        override val source: List<AttachmentContainer>?
            get() = null

        override suspend fun loadVideo() = downloadUser.videos.getVideo(contentId)
    }

    private inner class FullVideoHolder(val video: VkVideo) :
        AbstractVideoHolder(video.publicId()) {

        override suspend fun video(): VkVideo {
            return video
        }

        override suspend fun loadFullVideo(): FullVideo {
            return FullVideo(
                video,
                attachmentId,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }

        override val source: List<AttachmentContainer>?
            get() = null
    }

    private inner class InfoVideoHolder(val video: VkVideo, override val source: List<AttachmentContainer>) :
        AbstractVideoHolder(video.publicId()), VideoLoader {
        override suspend fun video() = video
        override suspend fun loadFullVideo(): FullVideo {
            val full = getVideoWithTryingLockBehavior(this)
            baseCheckVideo(full.video)

            return full
        }

        override suspend fun loadVideo(): VkVideo {
            val wall = source.firstOrNull { it is Wall }
            if (wall is Wall) {
                val downloadUserWall = downloadUser.walls.getById("${wall.ownerId}_${wall.id}")
                val wallVideo = messageScanner.scanForAttachment(downloadUserWall) { it.video }
                if (wallVideo != null) {
                    if (wallVideo.publicId() != this.video.publicId()) throw ContentNotFoundException()
                    wallVideo.checkRestriction()

                    return wallVideo
                }
            }
            return downloadUser.videos.getVideo(contentId)
        }
    }

    private fun baseCheckStory(story: VkStory) {
        if (!story.canSee!!)
            throw CantSeeStoryException()
        if (story.type != VkStory.Type.VIDEO)
            throw StoryNotVideoException()
    }

    private suspend fun findStoryById(id: String): VkStory {
        return downloadUser.stories.getById(id)
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

        override val source: List<AttachmentContainer>?
            get() = null

        override suspend fun loadVideo(): VkVideo {
            val story = findStoryById(contentId)
            baseCheckStory(story)

            return story.video!!
        }
    }

    // Тип держит историю которая пришла в личку группы
    private inner class GroupStoryHolder(
        val story: VkStory, override val source: List<AttachmentContainer>?
    ) : AbstractStoryHolder(story.normalId()) {

        override suspend fun video(): VkVideo {
            return story.video!!
        }

        override suspend fun loadFullVideo(): FullVideo {
            baseCheckStory(story)
            return FullVideo(
                story.video!!,
                attachmentId,
                null,
                videoAccessorFactory,
                workUserGroupService
            )
        }
    }
}