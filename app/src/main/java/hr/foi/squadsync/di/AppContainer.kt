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

class AppContainer(context: Context) {
    // Ako su Supabase kljucevi upisani u local.properties, koristi se Supabase.
    // Inace aplikacija radi s lokalnim racunima kao i prije.
    private val authDataSource: AuthDataSource = if (SupabaseProvider.isConfigured()) {
        SupabaseAuthDataSource(SupabaseProvider.createClient())
    } else {
        LocalAuthDataSource(
            EncryptedAccountStore(EncryptedFileStore(context, "auth_accounts.enc"))
        )
    }
    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(EncryptedFileStore(context, "auth_session.enc"))
    )
}
