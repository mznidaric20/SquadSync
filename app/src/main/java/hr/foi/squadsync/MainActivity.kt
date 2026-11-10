package hr.foi.squadsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import hr.foi.squadsync.ui.theme.SquadSyncTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            SquadSyncTheme {

                var trenutniEkran by rememberSaveable {
                    mutableStateOf("login")
                }

                // Povratak na prijavu tipkom Nazad

                BackHandler(
                    enabled = trenutniEkran == "registration"
                ) {
                    trenutniEkran = "login"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (trenutniEkran) {

                        "login" -> {

                            LoginScreen(
                                modifier = Modifier.padding(innerPadding),

                                onRegistrationClick = {
                                    trenutniEkran = "registration"
                                }
                            )
                        }

                        "registration" -> {

                            RegistrationScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}