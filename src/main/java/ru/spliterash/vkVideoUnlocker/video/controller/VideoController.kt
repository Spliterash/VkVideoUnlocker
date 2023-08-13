package ru.spliterash.vkVideoUnlocker.video.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.server.types.files.StreamedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.spliterash.vkVideoUnlocker.video.service.VideoService

@Controller
class VideoController(
    private val videoService: VideoService,
) {
    @Get("/videos/download/{id}")
    suspend fun download(
        @PathVariable("id") id: String,
        @Header("Range", defaultValue = "") rangeHeader: String?,
    ): HttpResponse<StreamedFile> = withContext(Dispatchers.IO) {
        val (accessor, _) = videoService.getInfoForDownload(id)
        val info = if (rangeHeader.isNullOrEmpty())
            accessor.load()
        else {
            accessor.load(rangeHeader)
        }

        return@withContext HttpResponse.status<StreamedFile>(HttpStatus.valueOf(info.code))
            .header("Accept-Ranges", "bytes")
            .apply {
                if (info.contentRange != null)
                    header("Content-Range", info.contentRange)
            }
            .body(StreamedFile(info.stream, MediaType("video/mp4"), 0, info.contentLength))
    }
}