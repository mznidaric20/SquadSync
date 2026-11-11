package hr.foi.squadsync.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Atomic, encrypted app-private storage. noBackupFilesDir also excludes device transfer. */
class EncryptedFileStore(
    context: Context,
    fileName: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val file = AtomicFile(File(context.noBackupFilesDir, fileName))
    private val mutex = Mutex()

    suspend fun read(): String? = withContext(dispatcher) {
        mutex.withLock {
            val bytes = try {
                file.openRead().use { it.readBytes() }
            } catch (exception: FileNotFoundException) {
                if (file.baseFile.exists()) throw exception
                return@withLock null
            }
            require(bytes.size > 1 + IV_LENGTH && bytes[0] == VERSION) { "Invalid encrypted file" }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(create = false), GCMParameterSpec(
                128, bytes.copyOfRange(1, 1 + IV_LENGTH)
            ))
            cipher.doFinal(bytes.copyOfRange(1 + IV_LENGTH, bytes.size)).toString(Charsets.UTF_8)
        }
    }

    suspend fun write(value: String) = withContext(dispatcher) {
        mutex.withLock {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
            val encrypted = byteArrayOf(VERSION) + cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val stream = file.startWrite()
            try {
                stream.write(encrypted)
                file.finishWrite(stream)
            } catch (exception: Exception) {
                file.failWrite(stream)
                throw exception
            }
        }
    }

    suspend fun clear() = withContext(dispatcher) {
        mutex.withLock {
            file.delete()
            if (file.baseFile.exists()) throw IOException("Could not remove encrypted file")
        }
    }

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_ALIAS = "squadsync.auth.v1"
        private const val IV_LENGTH = 12
        private const val VERSION: Byte = 1

        @Synchronized
        private fun key(create: Boolean): SecretKey {
            val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            (keystore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
            check(create) { "Encryption key is unavailable" }
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(KeyGenParameterSpec.Builder(
                    KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build())
            }.generateKey()
        }
    }
}
