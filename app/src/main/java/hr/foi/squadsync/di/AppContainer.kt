package hr.foi.squadsync.di

import android.content.Context
import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.data.local.LocalTeamJoinRepository
import hr.foi.squadsync.data.remote.SupabaseAuthDataSource
import hr.foi.squadsync.data.remote.SupabaseProvider
import hr.foi.squadsync.data.remote.SupabaseTeamJoinRepository
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.domain.teams.TeamJoinRepository
import io.github.jan.supabase.SupabaseClient

class AppContainer(context: Context) {
    // Ako su Supabase kljucevi upisani u local.properties, koristi se Supabase.
    // Inace aplikacija radi s lokalnim racunima kao i prije.
    // Svi dijelovi koriste isti klijent jer on cuva sesiju prijavljenog korisnika.
    private val supabaseClient: SupabaseClient? = if (SupabaseProvider.isConfigured()) {
        SupabaseProvider.createClient()
    } else {
        null
    }

    private val authDataSource: AuthDataSource = if (supabaseClient != null) {
        SupabaseAuthDataSource(supabaseClient)
    } else {
        LocalAuthDataSource(
            EncryptedAccountStore(EncryptedFileStore(context, "auth_accounts.enc"))
        )
    }
    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(EncryptedFileStore(context, "auth_session.enc"))
    )

    val teamJoinRepository: TeamJoinRepository = if (supabaseClient != null) {
        SupabaseTeamJoinRepository(supabaseClient)
    } else {
        LocalTeamJoinRepository()
    }
}
