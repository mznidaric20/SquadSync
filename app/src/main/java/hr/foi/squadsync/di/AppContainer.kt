package hr.foi.squadsync.di

import android.content.Context
import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.data.teams.local.EncryptedTeamStore
import hr.foi.squadsync.data.teams.local.LocalTeamDataSource
import hr.foi.squadsync.domain.teams.TeamRepository

class AppContainer(context: Context) {
    private val accountStore = EncryptedAccountStore(EncryptedFileStore(context, "auth_accounts.enc"))
    private val authDataSource: AuthDataSource = LocalAuthDataSource(accountStore)
    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(EncryptedFileStore(context, "auth_session.enc"))
    )
    private val teamDataSource: TeamDataSource = LocalTeamDataSource(
        EncryptedTeamStore(EncryptedFileStore(context, "teams.enc")), accountStore
    )
    val teamRepository: TeamRepository = DefaultTeamRepository(authRepository, teamDataSource)
}
