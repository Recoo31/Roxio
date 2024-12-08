package kurd.reco.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

fun isProxyDetected(): Boolean {
    val proxyHost = System.getProperty("http.proxyHost")
    return proxyHost != null && proxyHost.isNotEmpty()
}

fun copyText(text: String, message: String, context: Context) {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText   ("", text))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}