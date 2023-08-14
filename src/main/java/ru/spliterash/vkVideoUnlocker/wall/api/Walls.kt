package ru.spliterash.vkVideoUnlocker.wall.api

import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.wall.exceptions.WallPostNotFoundException

interface Walls {
    @Throws(WallPostNotFoundException::class)
    suspend fun getById(id: String): SomethingWithAttachments
}