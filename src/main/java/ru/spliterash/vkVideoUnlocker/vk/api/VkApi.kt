package ru.spliterash.vkVideoUnlocker.vk.api

import ru.spliterash.vkVideoUnlocker.group.api.Groups
import ru.spliterash.vkVideoUnlocker.message.Messages
import ru.spliterash.vkVideoUnlocker.video.api.Videos

interface VkApi {
    val id: Int

    val videos: Videos
    val messages: Messages
    val groups: Groups
}