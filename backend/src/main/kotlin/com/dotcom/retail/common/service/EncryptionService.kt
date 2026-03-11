package com.dotcom.retail.common.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(
    @Value("\${encryption.secret-key}") private val secretKey: String,
) {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
        private const val IV_LENGTH_BYTE = 12
    }

    fun encrypt(plaintext: String): String {
        val iv = ByteArray(IV_LENGTH_BYTE).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH_BIT, iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(ciphertext: String): String {
        val combined = Base64.getDecoder().decode(ciphertext)
        val iv = combined.copyOfRange(0, IV_LENGTH_BYTE)
        val encrypted = combined.copyOfRange(IV_LENGTH_BYTE, combined.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH_BIT, iv))
        return String(cipher.doFinal(encrypted))
    }

    private fun getKey(): SecretKeySpec {
        val keyBytes = Base64.getDecoder().decode(secretKey)
        return SecretKeySpec(keyBytes, "AES")
    }

}