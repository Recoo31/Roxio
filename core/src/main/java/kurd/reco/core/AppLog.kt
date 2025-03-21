package kurd.reco.core

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class LogItem(val message: String, val type: LogType)

enum class LogType {
    INFO, WARNING, ERROR, DEBUG
}

object AppLog {
    var logs by mutableStateOf(emptyList<LogItem>())

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        addLog(LogItem(message = "INFO: $message", type = LogType.INFO))
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        addLog(LogItem(message = "DEBUG: $message", type = LogType.DEBUG))
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
        addLog(LogItem(message = "ERROR: $message", type = LogType.ERROR))
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        addLog(LogItem(message = "WARNING: $message", type = LogType.WARNING))
    }

    fun v(tag: String, message: String) {
        Log.v(tag, message)
        addLog(LogItem(message = "VERBOSE: $message", type = LogType.INFO))
    }

    fun wtf(tag: String, message: String) {
        Log.wtf(tag, message)
        addLog(LogItem(message = "WTF: $message", type = LogType.INFO))
    }

    private fun addLog(logItem: LogItem) {
        logs = logs + logItem
    }
}

fun shareLogs(context: Context) {
    val logMessages = AppLog.logs.joinToString(separator = "\n") { log ->
        "[${log.type}] ${log.message}"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, logMessages)
    }
    val shareIntent = Intent.createChooser(intent, null)
    context.startActivity(shareIntent)
}
