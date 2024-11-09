package ru.spliterash.vkVideoUnlocker.common.exceptions

interface ReUploadRestrictionException : AlwaysNotifyException {
    val restrictionName: String
}