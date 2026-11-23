package hr.foi.squadsync.data.remote

import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.domain.teams.TeamJoinException
import hr.foi.squadsync.domain.teams.TeamJoinRepository
import hr.foi.squadsync.domain.validation.AccessCodeValidator
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseTeamJoinRepository(private val client: SupabaseClient) : TeamJoinRepository {

    override suspend fun joinTeam(accessCode: String): String {
        try {
            // Funkcija u bazi sama provjerava ulogu, kod i dvostruko clanstvo.
            val result = client.postgrest.rpc(
                "pridruzi_se_momcadi",
                buildJsonObject {
                    put("kod", AccessCodeValidator.normalize(accessCode))
                }
            )
            return result.decodeAs<String>()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: PostgrestRestException) {
            // U polju error je tocno poruka iz baze, npr. NEISPRAVAN_KOD.
            throw TeamJoinException(SupabaseMapper.teamJoinErrorFromMessage(exception.error), exception)
        } catch (exception: Exception) {
            // Nema interneta ili druga greska posluzitelja.
            throw TeamJoinException(TeamJoinError.SERVICE_UNAVAILABLE, exception)
        }
    }
}
