package com.yelloco.fingodriver

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal object FingoFactory
{
    val DEFAULT_SCOPE = CoroutineScope(Dispatchers.Default + CoroutineName("DefaultBackgroundDispatcher"))

    val IO_SCOPE = CoroutineScope(Dispatchers.Default + CoroutineName("IOScope"))

    val MAIN_SCOPE = CoroutineScope(Dispatchers.Main + CoroutineName("MainDispatcher"))

    object Constants
    {
        // Integers
        const val HALF_SECOND = 500
        const val ONE_SECOND = 1000
        const val THREE_SECONDS = 3000
        const val TEN_SECS = 10000
        const val TWENTY_SECS = 20000

        // Strings
        const val FINGO_DEVICE_NAME = "Finger Vein Module"
        const val KAN_UNIQUE_ID = "09032021KANProject"
    }
}