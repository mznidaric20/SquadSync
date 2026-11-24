package hr.foi.squadsync.data.remote

import android.util.Log
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.CreateTeamError
import hr.foi.squadsync.domain.teams.CreateTeamException
import hr.foi.squadsync.domain.teams.CreatedTeam
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException

class SupabaseTeamListRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun getMyTeams(): List<CreatedTeam> {
        val session = (
                authRepository.sessionState.value
                        as? SessionState.Authenticated
                )?.session ?: throw CreateTeamException(
            CreateTeamError.SESSION_REQUIRED
        )

        if (session.user.role != UserRole.COACH) {
            throw CreateTeamException(
                CreateTeamError.ACCESS_DENIED
            )
        }

        try {
            client.auth.awaitInitialization()

            val userId = client.auth.currentUserOrNull()?.id
                ?: throw CreateTeamException(
                    CreateTeamError.SESSION_REQUIRED
                )

            if (userId != session.user.id) {
                throw CreateTeamException(
                    CreateTeamError.SESSION_REQUIRED
                )
            }

            val rows = client.from("momcadi")
                .select(
                    columns = Columns.list(
                        "id",
                        "naziv",
                        "opis",
                        "trener_id",
                        "pristupni_kod"
                    )
                ) {
                    filter {
                        eq("trener_id", userId)
                    }
                }
                .decodeList<MomcadRow>()

            return rows.map { row ->
                CreatedTeam(
                    id = row.id,
                    name = row.naziv,
                    description = row.opis,
                    coachId = row.trener_id,
                    accessCode = row.pristupni_kod
                )
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: CreateTeamException) {
            throw e
        } catch (e: Exception) {
            Log.e(
                "SquadSyncTeam",
                "Greska pri dohvatu momcadi",
                e
            )

            throw CreateTeamException(
                CreateTeamError.DATA_UNAVAILABLE,
                e
            )
        }
    }
}