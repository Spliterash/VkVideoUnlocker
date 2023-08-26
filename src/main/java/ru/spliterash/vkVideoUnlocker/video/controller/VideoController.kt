package ru.spliterash.vkVideoUnlocker.video.controller

import com.github.benmanes.caffeine.cache.Caffeine
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.server.types.files.StreamedFile
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.video.Routes
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.dto.FullVideo
import ru.spliterash.vkVideoUnlocker.video.service.VideoService
import java.util.concurrent.TimeUnit

@Controller
class VideoController(
    private val videoService: VideoService,
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

    @Get(Routes.DOWNLOAD)
    suspend fun download(
        @PathVariable("attachmentId") attachmentId: String,
        @Header("Range", defaultValue = "") rangeHeader: String?,
    ): HttpResponse<StreamedFile> {
        val accessor = cache.get(attachmentId).await()

        val info = if (rangeHeader.isNullOrEmpty())
            accessor.load()
        else {
            accessor.load(rangeHeader)
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