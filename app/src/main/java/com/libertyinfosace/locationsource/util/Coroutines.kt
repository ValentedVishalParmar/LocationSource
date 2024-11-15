package com.libertyinfosace.locationsource.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object Coroutines {
    fun <T : Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit), onError: (Throwable) -> Unit) = CoroutineScope(
        Dispatchers.Main).launch {
        try {
            val data = CoroutineScope(Dispatchers.IO).async rt@{
                return@rt work()
            }.await()
            callback(data)
        } catch (e: Exception) {
            onError(e)
        }
    }
}