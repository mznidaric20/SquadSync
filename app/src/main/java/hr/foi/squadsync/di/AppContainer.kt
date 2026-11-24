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
import hr.foi.squadsync.data.remote.SupabaseCreateTeamRepository
import hr.foi.squadsync.data.remote.SupabaseTeamListRepository
import hr.foi.squadsync.data.teams.DefaultTeamRepository
import hr.foi.squadsync.data.teams.TeamDataSource
import hr.foi.squadsync.data.teams.local.EncryptedTeamStore
import hr.foi.squadsync.data.teams.local.LocalTeamDataSource
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamRepository
import io.github.jan.supabase.SupabaseClient

class AppContainer(context: Context) {

    private val accountStore: LocalAccountStore =
        EncryptedAccountStore(
            EncryptedFileStore(context, "auth_accounts.enc")
        )

    // Supabase klijent
    private val supabaseClient: SupabaseClient? =
        if (SupabaseProvider.isConfigured()) {
            SupabaseProvider.createClient()
        } else {
            null
        }

    // Odabir lokalne ili Supabase autentifikacije
    private val authDataSource: AuthDataSource =
        if (supabaseClient != null) {
            SupabaseAuthDataSource(supabaseClient)
        } else {
            LocalAuthDataSource(accountStore)
        }

    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(
            EncryptedFileStore(context, "auth_session.enc")
        )
    )

    // Lokalni izvor podataka za pregled članova momčadi
    private val teamDataSource: TeamDataSource = LocalTeamDataSource(
        teamStore = EncryptedTeamStore(
            EncryptedFileStore(context, "teams.enc")
        ),
        accountStore = accountStore
    )

    // Kreiranje momčadi
    val createTeamRepository: CreateTeamRepository? =
        supabaseClient?.let {
            SupabaseCreateTeamRepository(
                client = it,
                authRepository = authRepository
            )
        }

    // Dohvat momčadi iz Supabase baze
    val teamListRepository: SupabaseTeamListRepository? =
        supabaseClient?.let {
            SupabaseTeamListRepository(
                client = it,
                authRepository = authRepository
            )
        }

    val teamRepository: TeamRepository =
        teamListRepository ?: DefaultTeamRepository(authRepository, teamDataSource)
}
