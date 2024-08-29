package ru.spliterash.vkVideoUnlocker.photo.api

import ru.spliterash.vkVideoUnlocker.common.InputStreamSource

interface Photos {
    suspend fun uploadPhoto(peerId: Long, source: InputStreamSource): String
}