package ru.spliterash.vkVideoUnlocker.exceptions

class AlreadyExistException(
    val cachedVideoId: String
) : RuntimeException()
