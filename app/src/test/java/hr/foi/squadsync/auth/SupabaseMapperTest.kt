package hr.foi.squadsync.auth

import hr.foi.squadsync.data.remote.SupabaseMapper
import hr.foi.squadsync.domain.auth.AuthError
import hr.foi.squadsync.domain.auth.UserRole
import org.junit.Assert.assertEquals
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
}
