package hr.foi.squadsync.data.remote

import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.AccessCodeGenerator
import hr.foi.squadsync.domain.teams.CreateTeamError
import hr.foi.squadsync.domain.teams.CreateTeamException
import hr.foi.squadsync.domain.teams.CreateTeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamRequest
import hr.foi.squadsync.domain.teams.CreatedTeam
import hr.foi.squadsync.domain.teams.validate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import android.util.Log

class SupabaseCreateTeamRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository
) : CreateTeamRepository {

    override suspend fun createTeam(
        request: CreateTeamRequest
    ): CreatedTeam {
        val validRequest = request.validate()

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

            val newTeam = NovaMomcadRow(
                naziv = validRequest.name,
                opis = validRequest.description,
                trener_id = userId,
                pristupni_kod = AccessCodeGenerator.generate()
            )

            val savedTeam = client.from("momcadi")
                .insert(newTeam) {
                    select(
                        columns = Columns.list(
                            "id",
                            "naziv",
                            "opis",
                            "trener_id",
                            "pristupni_kod"
                        )
                    )
                }
                .decodeSingle<MomcadRow>()

            return CreatedTeam(
                id = savedTeam.id,
                name = savedTeam.naziv,
                description = savedTeam.opis,
                coachId = savedTeam.trener_id,
                accessCode = savedTeam.pristupni_kod
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: CreateTeamException) {
            throw e
        } catch (e: Exception) {
            Log.e("SquadSyncTeam", "Greska pri spremanju momcadi", e)

            throw CreateTeamException(
                CreateTeamError.DATA_UNAVAILABLE,
                e
            )
        }
    }
}