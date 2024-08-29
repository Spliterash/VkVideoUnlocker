package ru.spliterash.vkVideoUnlocker.docs.api

import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import java.io.File

interface Docs {
    suspend fun uploadAudioMessage(peerId:Long,info: InputStreamSource.Info): String
}