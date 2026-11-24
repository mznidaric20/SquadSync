package hr.foi.squadsync.di

import android.content.Context
import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedAccountStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.data.remote.SupabaseAuthDataSource
import hr.foi.squadsync.data.remote.SupabaseProvider
import hr.foi.squadsync.domain.auth.AuthRepository
import hr.foi.squadsync.data.remote.SupabaseCreateTeamRepository
import hr.foi.squadsync.domain.teams.CreateTeamRepository
import io.github.jan.supabase.SupabaseClient
import hr.foi.squadsync.data.remote.SupabaseTeamListRepository


class AppContainer(context: Context) {
    // Ako su Supabase kljucevi upisani u local.properties, koristi se Supabase.
    // Inace aplikacija radi s lokalnim racunima kao i prije.

    private val supabaseClient: SupabaseClient? =
        if (SupabaseProvider.isConfigured()) {
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

    val createTeamRepository: CreateTeamRepository? =
        supabaseClient?.let {
            SupabaseCreateTeamRepository(
                client = it,
                authRepository = authRepository
            )
        }

    val teamListRepository: SupabaseTeamListRepository? =
        supabaseClient?.let {
            SupabaseTeamListRepository(
                client = it,
                authRepository = authRepository
            )
        }


}
