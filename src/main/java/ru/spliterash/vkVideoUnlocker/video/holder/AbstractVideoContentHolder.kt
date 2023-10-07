package ru.spliterash.vkVideoUnlocker.video.holder

import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo

abstract class AbstractVideoContentHolder : VideoContentHolder {
    protected var fullVideo: FullVideo? = null

    protected abstract suspend fun loadFullVideo(): FullVideo
    final override suspend fun fullVideo(): FullVideo {
        return fullVideo ?: loadFullVideo().also {
            this.fullVideo = it
        }
    }
}