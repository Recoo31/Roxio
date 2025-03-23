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

    suspend fun startProxy(): Int {
        val fd = createSocket()
        if (fd < 0) {
            return -1 // TODO: should be error code
        }
        return jniStartProxy(fd)
    }

    suspend fun stopProxy(): Int {
        mutex.withLock {
            if (fd < 0) {
                throw IllegalStateException("Proxy is not running")
            }

            val result = jniStopProxy(fd)
            if (result == 0) {
                fd = -1
            }
            return result
        }
    }

    private suspend fun createSocket(): Int =
        mutex.withLock {
            if (fd >= 0) {
                throw IllegalStateException("Proxy is already running")
            }
            val argsStr = "-p 8118 -o1 -o25+s -T3 -At --tlsrec 1+s"

            val args = arrayOf("ciadpi") + shellSplit(argsStr)
            val fd = jniCreateSocketWithCommandLine(args)
            if (fd < 0) {
                return -1
            }
            this.fd = fd
            fd
        }

    private external fun jniCreateSocketWithCommandLine(args: Array<String>): Int

    private external fun jniStartProxy(fd: Int): Int

    private external fun jniStopProxy(fd: Int): Int
}

private fun shellSplit(string: CharSequence): List<String> {
    val tokens: MutableList<String> = ArrayList()
    var escaping = false
    var quoteChar = ' '
    var quoting = false
    var lastCloseQuoteIndex = Int.MIN_VALUE
    var current = StringBuilder()

    for (i in string.indices) {
        val c = string[i]

        if (escaping) {
            current.append(c)
            escaping = false
        } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
            escaping = true
        } else if (quoting && c == quoteChar) {
            quoting = false
            lastCloseQuoteIndex = i
        } else if (!quoting && (c == '\'' || c == '"')) {
            quoting = true
            quoteChar = c
        } else if (!quoting && Character.isWhitespace(c)) {
            if (current.isNotEmpty() || lastCloseQuoteIndex == i - 1) {
                tokens.add(current.toString())
                current = StringBuilder()
            }
        } else {
            current.append(c)
        }
    }

    if (current.isNotEmpty() || lastCloseQuoteIndex == string.length - 1) {
        tokens.add(current.toString())
    }

    return tokens
}