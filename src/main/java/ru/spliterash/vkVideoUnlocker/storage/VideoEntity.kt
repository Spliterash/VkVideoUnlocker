package ru.spliterash.vkVideoUnlocker.storage

class VideoEntity(
    /**
     * VK ID
     */
    val id: String,
    val status: Status,
    val unlockedId: String? = null,
) {
    enum class Status {
        /**
         * Видео является открытым, нет смысла его разблокивать
         */
        OPEN,

        /**
         * Мы уже его разблокировали
         */
        UNLOCKED
    }
}