package hr.foi.squadsync.data.local

import hr.foi.squadsync.data.auth.SessionStore
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthSession
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

class EncryptedSessionStore(private val storage: EncryptedFileStore) : SessionStore {
    override suspend fun read(): AuthSession? = try {
        storage.read()?.let { content ->
            val json = JSONObject(content)
            require(json.getInt("version") == 1)
            AuthSession(
                user = json.getJSONObject("user").toUser(),
                accessToken = json.getString("accessToken"),
                expiresAtEpochMillis = json.getLong("expiresAt")
            )
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        throw AuthException(AuthError.STORAGE_UNAVAILABLE, exception)
    } catch (exception: Exception) {
        // Corrupt data or a missing Keystore key requires a fresh sign-in.
        throw AuthException(AuthError.SESSION_EXPIRED, exception)
    }

    override suspend fun write(session: AuthSession) {
        storage.write(JSONObject()
            .put("version", 1)
            .put("user", session.user.toJson())
            .put("accessToken", session.accessToken)
            .put("expiresAt", session.expiresAtEpochMillis)
            .toString())
    }

    override suspend fun clear() = storage.clear()
}
