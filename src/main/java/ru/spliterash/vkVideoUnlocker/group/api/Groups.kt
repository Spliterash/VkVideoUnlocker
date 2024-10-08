package ru.spliterash.vkVideoUnlocker.group.api

import ru.spliterash.vkVideoUnlocker.group.dto.GroupInfo
import ru.spliterash.vkVideoUnlocker.group.vkModels.LongPollServerResponse

interface Groups {
    suspend fun status(groupId: Long): GroupInfo
    suspend fun join(groupId: Long)

    suspend fun getLongPollServer(): LongPollServerResponse

}
