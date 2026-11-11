package hr.foi.squadsync.data.local

import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import org.json.JSONObject

internal fun UserProfile.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("firstName", firstName)
    .put("lastName", lastName)
    .put("email", email)
    .put("role", role.name)

internal fun JSONObject.toUser(): UserProfile = UserProfile(
    id = getString("id").also { require(it.isNotBlank()) },
    firstName = getString("firstName"),
    lastName = getString("lastName"),
    email = getString("email").also { require(it.isNotBlank()) },
    role = UserRole.valueOf(getString("role"))
)
