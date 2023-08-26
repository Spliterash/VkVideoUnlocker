package ru.spliterash.vkVideoUnlocker.story.api

import ru.spliterash.vkVideoUnlocker.story.vkModels.VkStory

interface Stories {
    suspend fun getById(storyId: String): VkStory
}