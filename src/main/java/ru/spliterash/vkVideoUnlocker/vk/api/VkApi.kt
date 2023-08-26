package ru.spliterash.vkVideoUnlocker.vk.api

import ru.spliterash.vkVideoUnlocker.group.api.Groups
import ru.spliterash.vkVideoUnlocker.message.api.Messages
import ru.spliterash.vkVideoUnlocker.video.api.Videos
import ru.spliterash.vkVideoUnlocker.wall.api.Walls

interface VkApi {
    val id: Int

    val videos: Videos
    val messages: Messages
    val groups: Groups
    val walls: Walls
}