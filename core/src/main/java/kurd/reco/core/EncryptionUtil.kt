package kurd.reco.core

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private val SECRET_KEY = BuildConfig.ENCRYPTION_KEY

    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(data: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encode(encryptedBytes)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(data: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decodedBytes = Base64.decode(data)
        return String(cipher.doFinal(decodedBytes))
    }
}