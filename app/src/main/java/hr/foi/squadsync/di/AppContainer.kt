package hr.foi.squadsync.di

import android.content.Context
import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAccountStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.data.remote.SupabaseAuthDataSource
import hr.foi.squadsync.data.remote.SupabaseProvider
import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.data.teams.local.EncryptedTeamStore
import hr.foi.squadsync.data.teams.local.LocalTeamDataSource
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.teams.TeamRepository

class AppContainer(context: Context) {
    // Lokalni fallback i ogledne momcadi koriste istu pohranu lokalnih racuna.
    private val accountStore: LocalAccountStore = EncryptedAccountStore(
        EncryptedFileStore(context, "auth_accounts.enc")
    )

    // Ako su Supabase kljucevi upisani u local.properties, koristi se Supabase.
    // Inace aplikacija radi s lokalnim racunima kao i prije.
    private val authDataSource: AuthDataSource = if (SupabaseProvider.isConfigured()) {
        SupabaseAuthDataSource(SupabaseProvider.createClient())
    } else {
        LocalAuthDataSource(accountStore)
    }
    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(EncryptedFileStore(context, "auth_session.enc"))
    )
    private val teamDataSource: TeamDataSource = LocalTeamDataSource(
        teamStore = EncryptedTeamStore(EncryptedFileStore(context, "teams.enc")),
        accountStore = accountStore
    )
    val teamRepository: TeamRepository = DefaultTeamRepository(authRepository, teamDataSource)
}
