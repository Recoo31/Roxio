package kurd.reco.core

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess


object FridaUtil {

    private val FRIDA_KEYWORDS = listOf("frida", "gum-js-loop")
    val showLogs = true

    private const val TAG = "FridaUtil"

    /**
     * Checks if Frida is running by searching the process list for suspicious keywords.
     * @return true if Frida is detected, false otherwise.
     */

    private fun detectHooking(): Boolean {
        val libraries: MutableSet<String> = mutableSetOf("")
        val mapsFilename: String = "/proc/" + Process.myPid() + "/maps"
        val file = File(mapsFilename)
        var n: Int
        var lineSubstring: String

        try {
            file.bufferedReader().forEachLine {
                if ((it.endsWith(".so") || it.endsWith(".jar"))) {
                    n = it.lastIndexOf(" ")
                    lineSubstring = it.substring(n + 1)
                    libraries.add(lineSubstring)
                }

            }
            for (library in libraries) {
                if (library.contains("XposedBridge.jar")) {
                    exitProcess(0)
                }
                if (library.contains("frida") || library.contains("LIBFRIDA")) {
                    exitProcess(0)
                }
            }

        } catch (e: Exception) {
            return false
        }
        return false
    }
    private fun isFridaInstalled(): Boolean {
        try {
            val fridaFile = File("/data/local/tmp/frida-server")
            if (fridaFile.exists()) {
                exitProcess(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun isFridaProcessRunning(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ps")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("frida", ignoreCase = true)) {
                    exitProcess(0)
                }
            }

            false
        } catch (e: Exception) {
            if (showLogs) {

                Log.e(TAG, "Error checking Frida processes", e)
            }
            false
        }
    }

    private fun isFridaLibrariesLoaded(): Boolean {
        return try {
            System.getProperty("java.library.path")?.let { libraryPath ->
                if (libraryPath.contains("frida", ignoreCase = true)) {
                    exitProcess(0)
                }
            }
            false
        } catch (e: Exception) {
            if (showLogs) {
                Log.e(TAG, "Error checking for Frida libraries", e)
            }
            false
        }
    }

    private fun isFridaFilesDetected(): Boolean {
        return try {
            val fridaPaths = listOf(
                "/data/local/tmp/frida-server",
                "/data/local/tmp/frida",
                "/data/local/tmp/fs",
                "/data/local/tmp/agent.so",
                "/data/local/tmp/libfrida.so",
                "/data/local/tmp/frida-gadget.so"
            )

            fridaPaths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    exitProcess(0)
                }
            }

            false
        } catch (e: Exception) {
            if (showLogs) {
                Log.e(TAG, "Error checking Frida files", e)
            }
            false
        }
    }


    private fun isFridaProcessRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in activityManager.runningAppProcesses) {
            if (processInfo.processName.contains("frida", ignoreCase = true)) {
                exitProcess(0)
            }
        }
        return false // Process is not running
    }

    private fun isFridaHooksDetected(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                mapsFile.forEachLine { line ->
                    if (line.contains("frida", ignoreCase = true)) {
                        exitProcess(0)
                    }
                }
            }
            false
        } catch (e: Exception) {
            if (showLogs) {
                Log.e(TAG, "Error checking memory for Frida hooks", e)
            }
            false
        }
    }

    fun isFridaEnabled(context: Context): Boolean {
        return detectHooking() || isFridaProcessRunning(context) ||  isFridaInstalled() || isFridaProcessRunning() || isFridaHooksDetected() || isFridaLibrariesLoaded()
    }
}