package ru.spliterash.vkVideoUnlocker.tiktok

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.api.ProgressMeter
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.UploadUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class TiktokService(
    @UploadUser private val uploadUser: VkApi,
    @GroupUser private val groupUser: VkApi,
    private val tiktokDownloader: TiktokDownloader,
    private val tiktokVideoRepository: TiktokVideoRepository
) {
    suspend fun getVkId(tiktokVideoUrl: String, progressMeter: ProgressMeter): String {
        val info = tiktokDownloader.download(tiktokVideoUrl)
        val video = tiktokVideoRepository.findVideo(info.id)
        if (video != null) return video.vkId

        val vkId = reUpload(info.id, info.accessor, progressMeter)

        tiktokVideoRepository.save(TiktokVideoEntity(info.id, vkId))

        return vkId
    }

    private suspend fun reUpload(number: String, accessor: VideoAccessor, progress: ProgressMeter) =
        uploadUser.videos.upload(
            groupUser.id,
            "tiktok-$number",
            false,
            accessor,
            progress
        )
}