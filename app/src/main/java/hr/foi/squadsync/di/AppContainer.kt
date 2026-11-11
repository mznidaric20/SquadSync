package hr.foi.squadsync.di

import android.content.Context
import hr.foi.squadsync.data.auth.AuthDataSource
import hr.foi.squadsync.data.auth.DefaultAuthRepository
import hr.foi.squadsync.data.local.EncryptedFileStore
import hr.foi.squadsync.data.local.EncryptedSessionStore
import hr.foi.squadsync.data.local.LocalAuthDataSource
import hr.foi.squadsync.domain.auth.AuthRepository

class AppContainer(context: Context) {
    // Replace only this adapter with an HTTP AuthDataSource when the API is ready.
    private val authDataSource: AuthDataSource = LocalAuthDataSource(
        EncryptedFileStore(context, "auth_accounts.enc")
    )
    val authRepository: AuthRepository = DefaultAuthRepository(
        authDataSource,
        EncryptedSessionStore(EncryptedFileStore(context, "auth_session.enc"))
    )
}
