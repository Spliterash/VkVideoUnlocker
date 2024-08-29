package ru.spliterash.vkVideoUnlocker.vk.api

import ru.spliterash.vkVideoUnlocker.docs.api.Docs
import ru.spliterash.vkVideoUnlocker.group.api.Groups
import ru.spliterash.vkVideoUnlocker.message.api.Messages
import ru.spliterash.vkVideoUnlocker.photo.api.Photos
import ru.spliterash.vkVideoUnlocker.story.api.Stories
import ru.spliterash.vkVideoUnlocker.video.api.Videos
import ru.spliterash.vkVideoUnlocker.wall.api.Walls

interface VkApi {
    val id: Int

    val videos: Videos
    val messages: Messages
    val groups: Groups
    val walls: Walls
    val stories: Stories
    val photos: Photos
    val docs: Docs
}