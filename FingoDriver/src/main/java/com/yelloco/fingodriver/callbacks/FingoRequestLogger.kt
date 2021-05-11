package com.yelloco.fingodriver.callbacks

interface FingoRequestLogger {
    fun onLogDataAvailable(data: String?)
}