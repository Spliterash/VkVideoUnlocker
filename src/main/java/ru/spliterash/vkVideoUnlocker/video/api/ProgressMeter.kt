package ru.spliterash.vkVideoUnlocker.video.api

fun interface ProgressMeter {
    fun onProgress(current: Long, total: Long)
}