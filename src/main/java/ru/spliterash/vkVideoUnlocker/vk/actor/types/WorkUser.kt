package ru.spliterash.vkVideoUnlocker.vk.actor.types

import jakarta.inject.Qualifier

/**
 * Пользователь для загрузки и отгрузки видео
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class WorkUser
