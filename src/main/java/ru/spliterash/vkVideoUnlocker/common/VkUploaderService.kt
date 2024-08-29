package ru.spliterash.vkVideoUnlocker.common

import jakarta.inject.Singleton
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.use
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.common.okHttp.executeAsync
import ru.spliterash.vkVideoUnlocker.video.api.ProgressMeter

@Singleton
class VkUploaderService(
    okHttpFactory: OkHttpFactory,
) {
    private val client = okHttpFactory.create().build()
    suspend fun upload(
        url: String,
        progressMeter: ProgressMeter? = null,
        field: String,
        fileName: String,
        info: InputStreamSource.Info
    ): String {
        return Request.Builder()
            .url(url)
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(field, fileName, object : RequestBody() {
                        override fun contentType(): MediaType {
                            return "image/png".toMediaType()
                        }

                        override fun writeTo(sink: BufferedSink) {
                            info.stream.use { `in` ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var completed = 0L
                                while (`in`.read(buffer).also { bytesRead = it } != -1) {
                                    sink.write(buffer, 0, bytesRead)
                                    completed += bytesRead

                                    progressMeter?.onProgress(completed, info.contentLength)
                                }
                            }
                        }

                        override fun contentLength(): Long {
                            return info.contentLength
                        }

                        override fun isOneShot(): Boolean {
                            return true
                        }
                    })
                    .build()
            )
            .build()
            .executeAsync(client)
            .use { it.body.string() }
    }
}