package kurd.reco.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ByeDpiProxy {
    companion object {
        init {
            System.loadLibrary("core")
        }
    }

    private val mutex = Mutex()
    private var fd = -1

    suspend fun startProxy(): Int = mutex.withLock {
        if (fd >= 0) {
            throw IllegalStateException("Proxy is already running")
        }

        val args = arrayOf("ciadpi", "-p", "8118", "-o1", "-o25+s", "-T3", "-At", "--tlsrec", "1+s")
        fd = jniCreateSocketWithCommandLine(args)
        if (fd < 0) return -1
        
        return jniStartProxy(fd)
    }

    suspend fun stopProxy(): Int = mutex.withLock {
        if (fd < 0) {
            throw IllegalStateException("Proxy is not running")
        }

        val result = jniStopProxy(fd)
        if (result == 0) {
            fd = -1
        }
        result
    }

    private external fun jniCreateSocketWithCommandLine(args: Array<String>): Int
    private external fun jniStartProxy(fd: Int): Int
    private external fun jniStopProxy(fd: Int): Int
}