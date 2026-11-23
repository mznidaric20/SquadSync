package hr.foi.squadsync

import hr.foi.squadsync.data.local.LocalTeamJoinRepository
import hr.foi.squadsync.domain.teams.TeamJoinError
import hr.foi.squadsync.domain.teams.TeamJoinException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalTeamJoinRepositoryTest {

    @Test
    fun givenLocalRepository_whenJoiningTeam_thenServiceUnavailableError() = runTest {
        val repository = LocalTeamJoinRepository()
        var error: TeamJoinError? = null

        try {
            repository.joinTeam("X7K2P4")
        } catch (exception: TeamJoinException) {
            error = exception.error
        }

        assertEquals(TeamJoinError.SERVICE_UNAVAILABLE, error)
    }
}
