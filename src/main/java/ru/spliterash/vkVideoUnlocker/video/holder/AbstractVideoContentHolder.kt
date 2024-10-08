package ru.spliterash.vkVideoUnlocker.video.holder

import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo

abstract class AbstractVideoContentHolder(
    val contentId: String
) : VideoContentHolder {
    protected var fullVideo: FullVideo? = null

    override val ownerId: Long
        get() {
            val contentId = contentId
            val split = contentId.split("_")

            return split[0].toLong()
        }

    protected abstract suspend fun loadFullVideo(): FullVideo
    final override suspend fun fullVideo(): FullVideo {
        return fullVideo ?: loadFullVideo().also {
            this.fullVideo = it
        }
    }
}