package ru.spliterash.vkVideoUnlocker.common

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CoroutineHelper {
    val scope = CoroutineScope(Dispatchers.IO +
            SupervisorJob() +
            CoroutineExceptionHandler { _, ex -> ex.printStackTrace() }
    )
}