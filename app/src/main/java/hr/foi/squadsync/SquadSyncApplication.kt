package hr.foi.squadsync

import android.app.Application
import hr.foi.squadsync.di.AppContainer

class SquadSyncApplication : Application() {
    val container by lazy { AppContainer(applicationContext) }
}
