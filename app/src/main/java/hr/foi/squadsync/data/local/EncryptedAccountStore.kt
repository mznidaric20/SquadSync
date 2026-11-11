package hr.foi.squadsync.data.local

import org.json.JSONArray
import org.json.JSONObject

class EncryptedAccountStore(private val storage: EncryptedFileStore) : LocalAccountStore {
    override suspend fun read(): List<LocalAccount> {
        val content = storage.read() ?: return emptyList()
        val json = JSONObject(content)
        require(json.getInt("version") == 1)
        val accounts = json.getJSONArray("accounts")
        return List(accounts.length()) { index ->
            val account = accounts.getJSONObject(index)
            LocalAccount(
                user = account.getJSONObject("user").toUser(),
                salt = account.getString("salt"),
                passwordHash = account.getString("passwordHash"),
                tokenHash = if (account.has("tokenHash")) account.getString("tokenHash") else null,
                expiresAtEpochMillis = if (account.has("expiresAt")) account.getLong("expiresAt") else null
            )
        }
    }

    override suspend fun write(accounts: List<LocalAccount>) {
        val records = accounts.map { account ->
            JSONObject().put("user", account.user.toJson())
                .put("salt", account.salt)
                .put("passwordHash", account.passwordHash)
                .apply {
                    account.tokenHash?.let { put("tokenHash", it) }
                    account.expiresAtEpochMillis?.let { put("expiresAt", it) }
                }
        }
        storage.write(JSONObject().put("version", 1).put("accounts", JSONArray(records)).toString())
    }
}
