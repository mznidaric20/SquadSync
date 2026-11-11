package hr.foi.squadsync

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.Modifier
import hr.foi.squadsync.ui.auth.SquadSyncApp
import hr.foi.squadsync.ui.theme.SquadNavy
import hr.foi.squadsync.ui.theme.SquadSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val repository = (application as SquadSyncApplication).container.authRepository
        setContent {
            SquadSyncTheme {
                Box(Modifier.fillMaxSize()) {
                    SquadSyncApp(repository)
                    Spacer(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(SquadNavy))
                }
            }
        }
    }
}
