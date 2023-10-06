package ru.spliterash.vkVideoUnlocker.common

fun <K, V> MutableMap<K, V>.putIfNotNull(key: K, value: V?) {
    if (value != null)
        put(key, value)
}