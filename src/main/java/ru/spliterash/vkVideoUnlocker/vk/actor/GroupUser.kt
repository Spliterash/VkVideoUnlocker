package ru.spliterash.vkVideoUnlocker.vk.actor

import jakarta.inject.Qualifier

/**
 * Пользователь для тыканья
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class GroupUser
