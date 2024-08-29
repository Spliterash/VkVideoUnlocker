package ru.spliterash.vkVideoUnlocker.ffmpeg

data class Platform(
    val os: OS,
    val arch: Arch,
) {
    enum class OS {
        WINDOWS,
        LINUX,
        MACOS,
        OTHER,
    }

    enum class Arch {
        X86,
        X86_64,
        ARM,
        ARM_64,
        OTHER
    }

    companion object {
        fun getPlatform(): Platform {
            val osName = System.getProperty("os.name").lowercase()
            val archName = System.getProperty("os.arch").lowercase()

            val os = when {
                osName.contains("windows") -> OS.WINDOWS
                osName.contains("linux") || osName.contains("freebsd") || osName.contains("sunos") || osName.contains("unix") -> OS.LINUX
                osName.contains("mac os x") || osName.contains("darwin") -> OS.MACOS
                else -> OS.OTHER
            }

            val arch = when (archName) {
                "x86", "i386", "i686" -> Arch.X86
                "amd64", "x86_64" -> Arch.X86_64
                "aarch32", "arm", "armv7", "armv7l" -> Arch.ARM
                "aarch64" -> Arch.ARM_64
                else -> Arch.OTHER
            }

            return Platform(os, arch)
        }
    }
}
