package ru.spliterash.vkVideoUnlocker.ffmpeg

import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import jakarta.inject.Singleton
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.notExists
import kotlin.io.path.setPosixFilePermissions

@Singleton
class FFmpegService {
    private val ffmpegPath: Path

    init {
        val platform = Platform.getPlatform()

        val ffmpegUrl: String
        val entryPath: String

        when {
            platform.os == Platform.OS.WINDOWS && platform.arch == Platform.Arch.X86_64 -> {
                ffmpegUrl =
                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
                entryPath = "ffmpeg-master-latest-win64-gpl/bin/ffmpeg.exe"
                ffmpegPath = Path.of("natives/ffmpeg.exe").toAbsolutePath()
            }

            platform.os == Platform.OS.LINUX && platform.arch == Platform.Arch.X86_64 -> {
                ffmpegUrl =
                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz"
                entryPath = "ffmpeg-master-latest-linux64-gpl/bin/ffmpeg"
                ffmpegPath = Path.of("natives/ffmpeg").toAbsolutePath()
            }

            platform.os == Platform.OS.LINUX && platform.arch == Platform.Arch.ARM_64 -> {
                ffmpegUrl =
                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linuxarm64-gpl.tar.xz"
                entryPath = "ffmpeg-master-latest-linuxarm64-gpl/bin/ffmpeg"
                ffmpegPath = Path.of("natives/ffmpeg").toAbsolutePath()
            }

            else -> throw IllegalStateException()
        }

        if (ffmpegPath.notExists()) {
            URI.create(ffmpegUrl).toURL().openStream().use { inputStream ->
                when (platform.os) {
                    Platform.OS.WINDOWS -> ZipArchiveInputStream(inputStream).use { zipIs ->
                        extractFileFromArchive(zipIs, entryPath, ffmpegPath)
                    }

                    Platform.OS.LINUX -> XZCompressorInputStream(inputStream).use { xzIs ->
                        TarArchiveInputStream(xzIs).use { tarIs ->
                            extractFileFromArchive(tarIs, entryPath, ffmpegPath)
                            ffmpegPath.setPosixFilePermissions(ffmpegPath.getPosixFilePermissions() + PosixFilePermission.OWNER_EXECUTE)
                        }
                    }

                    else -> throw IllegalStateException()
                }
            }
        }
    }

    private fun extractFileFromArchive(archiveInputStream: ArchiveInputStream<*>, entryPath: String, result: Path) {
        while (true) {
            val entry = archiveInputStream.nextEntry ?: break

            if (entry.name == entryPath) {
                val outputFile = result.toFile()
                outputFile.parentFile?.mkdirs() // Создать необходимые директории
                FileOutputStream(outputFile).use { outputStream ->
                    archiveInputStream.copyTo(outputStream)
                }
                return
            }
        }

        throw IOException("File $entryPath not found in the archive")
    }


    fun getFFmpeg(): FFmpeg {
        return FFmpeg(ffmpegPath)
    }
}
