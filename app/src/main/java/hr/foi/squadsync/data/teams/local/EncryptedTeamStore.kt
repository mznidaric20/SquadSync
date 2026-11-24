package hr.foi.squadsync.data.teams.local

import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.TeamRoster
import org.json.JSONArray
import org.json.JSONObject

class EncryptedTeamStore(private val storage: EncryptedFileStore) : TeamStore {
    override suspend fun read(): List<TeamRoster>? {
        val content = storage.read() ?: return null
        val root = JSONObject(content)
        require(root.getInt("version") == 1)
        val records = root.getJSONArray("teams")
        return List(records.length()) { index ->
            val record = records.getJSONObject(index)
            val memberRecords = record.getJSONArray("members")
            val members = List(memberRecords.length()) { memberIndex ->
                val member = memberRecords.getJSONObject(memberIndex)
                TeamMember(member.getString("id"), member.getString("firstName"),
                    member.getString("lastName"), member.getString("email"))
            }
            TeamRoster(
                Team(
                    id = record.getString("id").also { require(it.isNotBlank()) },
                    name = record.getString("name"),
                    sport = record.getString("sport"),
                    coachId = record.getString("coachId").also { require(it.isNotBlank()) },
                    memberCount = members.size
                ), members
            )
        }
    }

    override suspend fun write(rosters: List<TeamRoster>) {
        val records = rosters.map { roster ->
            JSONObject().put("id", roster.team.id).put("name", roster.team.name)
                .put("sport", roster.team.sport).put("coachId", roster.team.coachId)
                .put("members", JSONArray(roster.members.map { member ->
                    JSONObject().put("id", member.id).put("firstName", member.firstName)
                        .put("lastName", member.lastName).put("email", member.email)
                }))
        }
        storage.write(JSONObject().put("version", 1).put("teams", JSONArray(records)).toString())
    }
}
