package ru.spliterash.vkVideoUnlocker.video.controller

import com.github.benmanes.caffeine.cache.Caffeine
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.server.types.files.StreamedFile
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.video.DownloadUrlSupplier
import ru.spliterash.vkVideoUnlocker.video.Routes
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.controller.response.VideoResponse
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import java.util.concurrent.TimeUnit

@Controller
class VideoController(
    private val videoService: VideoService,
    private val downloadUrlSupplier: DownloadUrlSupplier,
    @Value("\${vk-unlocker.domain}") private val domain: String
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val cache = Caffeine
        .newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build<String, Deferred<VideoAccessor>> {
            scope.async {
                load(it).toAccessor()
            }
        }

    private suspend fun load(attachmentId: String): FullVideo {
        val holder = videoService
            .wrapAttachmentId(attachmentId)
            ?: throw RuntimeException("Unknown attachment type in $attachmentId")

        return holder.fullVideo()
    }

    @Get(Routes.INFO)
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun info(
        @PathVariable("attachmentId") attachmentId: String,
    ): VideoResponse {
        val accessor = cache.get(attachmentId).await()
        val url = downloadUrlSupplier.downloadUrl(attachmentId)

        return VideoResponse(
            accessor.maxQuality,
            accessor.preview(),
            url
        )
    }

    @Get(Routes.DOWNLOAD)
    suspend fun download(
        @PathVariable("attachmentId") attachmentId: String,
        @QueryValue("quality", defaultValue = "") quality: String,
        @Header("Range", defaultValue = "") rangeHeader: String?,
    ): HttpResponse<StreamedFile> {
        val accessor = cache.get(attachmentId).await()
        val qualityInt = try {
            quality.toInt()
        } catch (ex: Exception) {
            accessor.maxQuality
        }

        val info = if (rangeHeader.isNullOrEmpty())
            accessor.load(qualityInt)
        else {
            accessor.load(qualityInt, rangeHeader)
        }

        return HttpResponse.status<StreamedFile>(HttpStatus.valueOf(info.code))
            .header("Accept-Ranges", "bytes")
            .apply {
                if (info.contentRange != null)
                    header("Content-Range", info.contentRange)
            }
            .body(StreamedFile(info.stream, MediaType("video/mp4"), 0, info.contentLength))
    }
}