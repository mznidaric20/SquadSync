package hr.foi.squadsync.data.remote

import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthDataSource(private val client: SupabaseClient) : AuthDataSource {

    override suspend fun register(request: RegistrationRequest): AuthSession {
        try {
            // Trigger u bazi iz ovih podataka stvara red u tablici korisnici.
            client.auth.signUpWith(Email) {
                email = request.email
                password = request.password
                data = buildJsonObject {
                    put("ime", request.firstName)
                    put("prezime", request.lastName)
                    put("uloga", SupabaseMapper.roleToDatabase(request.role))
                }
            }
            return createSession()
        } catch (exception: Exception) {
            throw toAuthException(exception)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthSession {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            return createSession()
        } catch (exception: Exception) {
            throw toAuthException(exception)
        }
    }

    override suspend fun fetchCurrentUser(session: AuthSession): UserProfile {
        try {
            // Supabase pri pokretanju ucitava spremljenu sesiju pa treba pricekati.
            client.auth.awaitInitialization()
            val userId = client.auth.currentUserOrNull()?.id
            if (userId == null || userId != session.user.id) {
                throw AuthException(AuthError.SESSION_EXPIRED)
            }
            return loadProfile(userId)
        } catch (exception: Exception) {
            throw toAuthException(exception)
        }
    }

    override suspend fun signOut(session: AuthSession) {
        try {
            client.auth.signOut()
        } catch (exception: Exception) {
            throw toAuthException(exception)
        }
    }

    private suspend fun createSession(): AuthSession {
        val supabaseSession = client.auth.currentSessionOrNull()
        val userId = client.auth.currentUserOrNull()?.id
        if (supabaseSession == null || userId == null) {
            throw AuthException(AuthError.SERVICE_UNAVAILABLE)
        }
        // Supabase sam obnavlja token, zato sesija u aplikaciji traje kao i kod lokalne prijave.
        return AuthSession(
            user = loadProfile(userId),
            accessToken = supabaseSession.accessToken,
            expiresAtEpochMillis = System.currentTimeMillis() + SESSION_LIFETIME_MILLIS
        )
    }

    private suspend fun loadProfile(userId: String): UserProfile {
        val row = client.from("korisnici")
            .select { filter { eq("id", userId) } }
            .decodeSingle<KorisnikRow>()
        return UserProfile(
            id = row.id,
            firstName = row.ime,
            lastName = row.prezime,
            email = row.email,
            role = SupabaseMapper.roleFromDatabase(row.uloga)
        )
    }

    // Supabase greske pretvara u nase AuthError vrijednosti.
    private fun toAuthException(exception: Exception): Exception {
        if (exception is CancellationException || exception is AuthException) {
            return exception
        }
        if (exception is AuthRestException) {
            return AuthException(SupabaseMapper.errorFromCode(exception.error), exception)
        }
        // Nema interneta, greska posluzitelja, profil nije pronaden...
        return AuthException(AuthError.SERVICE_UNAVAILABLE, exception)
    }

    companion object {
        // Koliko dugo aplikacija smatra korisnika prijavljenim: 30 dana u milisekundama.
        // Supabase token istjece nakon sat vremena, ali ga Supabase sam obnavlja,
        // pa za aplikaciju koristimo isto trajanje kao lokalna prijava.
        private const val SESSION_LIFETIME_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
