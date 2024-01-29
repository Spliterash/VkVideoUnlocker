package ru.spliterash.vkVideoUnlocker.common

import kotlinx.coroutines.launch
import ru.spliterash.vkVideoUnlocker.common.SizeFormat.toHumanSizeReadable
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.video.api.ProgressMeter

class MessageNotificationProgressMeter(
    private val editableMessage: EditableMessage
) : ProgressMeter {
    private var prev = 0L
    private var buf = 0L
    override fun onProgress(current: Long, total: Long) {
        val changeTo = current - prev
        buf += changeTo
        prev = current
        if (buf > 1024 * 1024 * 5) { // Каждые 5 метров
            buf = 0
            CoroutineHelper.scope.launch {
                editableMessage.sendOrUpdate("Загрузка (${current.toHumanSizeReadable()}/${total.toHumanSizeReadable()})")
            }
        }
    }
}