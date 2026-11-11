package hr.foi.squadsync.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher {
    fun newSalt(): String = Base64.getEncoder().encodeToString(ByteArray(16).also {
        SecureRandom().nextBytes(it)
    })

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 120_000, 256)
        return try {
            Base64.getEncoder().encodeToString(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            )
        } finally {
            spec.clearPassword()
        }
    }

    fun matches(password: String, salt: String, expected: String): Boolean = MessageDigest.isEqual(
        Base64.getDecoder().decode(hash(password, salt)),
        Base64.getDecoder().decode(expected)
    )
}
