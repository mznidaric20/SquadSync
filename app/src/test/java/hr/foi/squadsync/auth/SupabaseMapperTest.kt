package hr.foi.squadsync.auth

import hr.foi.squadsync.data.remote.SupabaseMapper
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.AuthException
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.teams.TeamJoinError
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SupabaseMapperTest {

    @Test
    fun givenCoachRole_whenMappedToDatabase_thenReturnsTrener() {
        assertEquals("Trener", SupabaseMapper.roleToDatabase(UserRole.COACH))
    }

    @Test
    fun givenAthleteRole_whenMappedToDatabase_thenReturnsSportas() {
        assertEquals("Sportaš", SupabaseMapper.roleToDatabase(UserRole.ATHLETE))
    }

    @Test
    fun givenTrenerFromDatabase_whenMapped_thenReturnsCoach() {
        assertEquals(UserRole.COACH, SupabaseMapper.roleFromDatabase("Trener"))
    }

    @Test
    fun givenSportasFromDatabase_whenMapped_thenReturnsAthlete() {
        assertEquals(UserRole.ATHLETE, SupabaseMapper.roleFromDatabase("Sportaš"))
    }

    @Test
    fun givenUnknownRole_whenMapped_thenFailsClosed() {
        try {
            SupabaseMapper.roleFromDatabase("Nepoznata uloga")
            fail("Unknown roles must be rejected")
        } catch (exception: AuthException) {
            assertEquals(AuthError.SERVICE_UNAVAILABLE, exception.error)
        }
    }

    @Test
    fun givenUserAlreadyExistsCode_whenMapped_thenReturnsEmailAlreadyUsed() {
        assertEquals(AuthError.EMAIL_ALREADY_USED, SupabaseMapper.errorFromCode("user_already_exists"))
    }

    @Test
    fun givenInvalidCredentialsCode_whenMapped_thenReturnsInvalidCredentials() {
        assertEquals(AuthError.INVALID_CREDENTIALS, SupabaseMapper.errorFromCode("invalid_credentials"))
    }

    @Test
    fun givenSessionNotFoundCode_whenMapped_thenReturnsSessionExpired() {
        assertEquals(AuthError.SESSION_EXPIRED, SupabaseMapper.errorFromCode("session_not_found"))
    }

    @Test
    fun givenUnknownCode_whenMapped_thenReturnsServiceUnavailable() {
        assertEquals(AuthError.SERVICE_UNAVAILABLE, SupabaseMapper.errorFromCode("something_else"))
    }

    @Test
    fun givenNoCode_whenMapped_thenReturnsServiceUnavailable() {
        assertEquals(AuthError.SERVICE_UNAVAILABLE, SupabaseMapper.errorFromCode(null))
    }

    @Test
    fun givenNotAthleteMessage_whenMapped_thenReturnsNotAthlete() {
        assertEquals(TeamJoinError.NOT_ATHLETE, SupabaseMapper.teamJoinErrorFromMessage("NIJE_SPORTAS"))
    }

    @Test
    fun givenInvalidCodeMessage_whenMapped_thenReturnsInvalidCode() {
        assertEquals(TeamJoinError.INVALID_CODE, SupabaseMapper.teamJoinErrorFromMessage("NEISPRAVAN_KOD"))
    }

    @Test
    fun givenAlreadyMemberMessage_whenMapped_thenReturnsAlreadyMember() {
        assertEquals(TeamJoinError.ALREADY_MEMBER, SupabaseMapper.teamJoinErrorFromMessage("VEC_CLAN"))
    }

    @Test
    fun givenUnknownMessage_whenMapped_thenReturnsServiceUnavailable() {
        assertEquals(TeamJoinError.SERVICE_UNAVAILABLE, SupabaseMapper.teamJoinErrorFromMessage("something_else"))
    }

    @Test
    fun givenNoMessage_whenMapped_thenReturnsServiceUnavailable() {
        assertEquals(TeamJoinError.SERVICE_UNAVAILABLE, SupabaseMapper.teamJoinErrorFromMessage(null))
    }
}
