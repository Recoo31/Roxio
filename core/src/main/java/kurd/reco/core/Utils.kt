package kurd.reco.core

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileReader
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.ZipFile
import kotlin.system.exitProcess

fun isVpnDetectedSimple(): Boolean {
    try {
        val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            if (networkInterface.isUp && (networkInterface.name.startsWith("tun") || networkInterface.name.startsWith("ppp"))) {
                return true // VPN is detected
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false // VPN is not detected
}


fun copyText(text: String, message: String, context: Context) {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText   ("", text))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}