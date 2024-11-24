package ru.spliterash.vkVideoUnlocker.tiktok

import com.github.kokorin.jaffree.StreamType
import com.github.kokorin.jaffree.ffmpeg.PipeInput
import com.github.kokorin.jaffree.ffmpeg.UrlOutput
import jakarta.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.ffmpeg.FFmpegService
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.api.ProgressMeter
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.UploadUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream

@Singleton
class TiktokService(
    @UploadUser private val uploadUser: VkApi,
    @GroupUser private val groupUser: VkApi,
    private val tiktokVideoDownloader: TiktokVideoDownloader,
    private val tiktokVideoRepository: TiktokVideoRepository,
    private val tiktokPhotoDownloader: TiktokPhotoDownloader,
    private val fFmpegService: FFmpegService,
) {
    suspend fun getVkId(tiktokVideoUrl: String, tiktokVideoId: String, progressMeter: ProgressMeter): String {
        val video = tiktokVideoRepository.findVideo(tiktokVideoId)
        if (video != null) return video.vkId
        val accessor = tiktokVideoDownloader.download(tiktokVideoUrl)
        val vkId = reUpload(tiktokVideoId, accessor, progressMeter)


        tiktokVideoRepository.save(TiktokVideoEntity(tiktokVideoId, vkId))
        return vkId
    }

    suspend fun getPhotoAttachmentIds(peerId: Long, tiktokPhotoUrl: String): TiktokPhotoVk = coroutineScope {
        val photo = tiktokPhotoDownloader.download(tiktokPhotoUrl)
        val voiceAttachment = if (photo.music == null) null else async { convertToVkVoice(peerId, photo.music) }

        val photoAttachments = photo.photos
            .map { async { groupUser.photos.uploadPhoto(peerId, it) } }

        TiktokPhotoVk(voiceAttachment?.await(), photoAttachments.awaitAll())
    }

    private suspend fun convertToVkVoice(peerId: Long, music: InputStreamSource): String {
        val stream = music.load().stream
        val output = Files.createTempFile("voiceoutput", "opus")
        try {
            val ffmpeg = fFmpegService.getFFmpeg()
            ffmpeg.addInput(PipeInput.pumpFrom(stream))
                .setOverwriteOutput(true)
                .addOutput(
                    UrlOutput.toPath(output)
                        .setFormat("opus")
                        .setCodec(StreamType.AUDIO, "libopus")
                        .addArguments("-ac", "1")
                        .addArguments("-t", "3600")
                        .addArguments("-b:a", "16k")
                        .addArguments("-ar", "16000")
                )
                .executeAsync()
                .await()
            return groupUser.docs.uploadAudioMessage(
                peerId,
                InputStreamSource.Info( // Немного кринжова, но мне впадлу переделывать
                    200,
                    output.inputStream(),
                    null,
                    output.fileSize()
                )
            )
        } finally {
            stream.close()
            output.deleteIfExists()
        }
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