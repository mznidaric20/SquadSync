package hr.foi.squadsync.data.remote

import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.auth.AuthSession
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.domain.teams.TeamException
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

class SupabaseTeamListRepository(
    client: SupabaseClient,
    authRepository: AuthRepository
) : TeamRepository by DefaultTeamRepository(authRepository, SupabaseTeamDataSource(client))

private class SupabaseTeamDataSource(private val client: SupabaseClient) : TeamDataSource {
    override suspend fun fetchTeams(session: AuthSession): List<Team> {
        requireSession(session)
        return client.from("momcadi").select(columns = TEAM_COLUMNS) {
            filter { eq("trener_id", session.user.id) }
        }.decodeList<MomcadRow>().map { it.toTeam() }
    }

    override suspend fun fetchTeam(session: AuthSession, teamId: String): Team? {
        requireSession(session)
        return client.from("momcadi").select(columns = TEAM_COLUMNS) {
            filter {
                eq("id", teamId)
                eq("trener_id", session.user.id)
            }
        }.decodeList<MomcadRow>().singleOrNull()?.toTeam()
    }

    override suspend fun fetchMembers(session: AuthSession, teamId: String): List<TeamMember> {
        requireSession(session)
        // Read membership through the owned team; RLS also applies to both nested tables.
        val row = client.from("momcadi").select(columns = Columns.raw(
            "id,clanovi_momcadi(korisnici(id,ime,prezime,email,uloga))"
        )) {
            filter {
                eq("id", teamId)
                eq("trener_id", session.user.id)
            }
        }.decodeList<MomcadMembersRow>().singleOrNull()
            ?: throw TeamException(TeamError.NOT_FOUND)

        return row.clanovi_momcadi.map { membership ->
            val member = membership.korisnici ?: throw TeamException(TeamError.DATA_UNAVAILABLE)
            TeamMember(member.id, member.ime, member.prezime, member.email)
        }.distinctBy { it.id }.sortedWith(compareBy({ it.lastName }, { it.firstName }, { it.id }))
    }

    private suspend fun requireSession(session: AuthSession) {
        client.auth.awaitInitialization()
        if (client.auth.currentUserOrNull()?.id != session.user.id) {
            throw TeamException(TeamError.SESSION_REQUIRED)
        }
    }

    private fun MomcadRow.toTeam() = Team(
        id = id,
        name = naziv,
        sport = "",
        coachId = trener_id,
        memberCount = 0,
        description = opis,
        accessCode = pristupni_kod
    )

    private companion object {
        val TEAM_COLUMNS = Columns.list("id", "naziv", "opis", "trener_id", "pristupni_kod")
    }
}

@Serializable
private data class MomcadMembersRow(val id: String, val clanovi_momcadi: List<ClanMomcadiRow>)

@Serializable
private data class ClanMomcadiRow(val korisnici: KorisnikRow?)
