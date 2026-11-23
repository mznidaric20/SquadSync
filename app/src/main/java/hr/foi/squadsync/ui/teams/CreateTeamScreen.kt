package hr.foi.squadsync.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.domain.teams.CreateTeamRequest
import hr.foi.squadsync.domain.teams.validate
import hr.foi.squadsync.ui.auth.CourtArtwork
import hr.foi.squadsync.ui.auth.SquadBrand
import hr.foi.squadsync.ui.theme.SquadCyan
import hr.foi.squadsync.ui.theme.SquadNavy
import hr.foi.squadsync.ui.theme.SquadSyncTheme

@Composable
fun CreateTeamScreen(
    onBack: () -> Unit,
    onCreate: (CreateTeamRequest) -> Unit,
    isSaving: Boolean = false,
    saveError: String? = null
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showNameError by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(SquadNavy, Color(0xFF0E3670))
                    )
                )
        ) {
            CourtArtwork(Modifier.matchParentSize())

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                SquadBrand()

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "UPRAVLJANJE MOMČADIMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = SquadCyan
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Nova momčad",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Kreiraj momčad i organiziraj svoje sportaše.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFBECEE6)
                )

                Spacer(Modifier.height(20.dp))
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-16).dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Podaci o momčadi",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Unesi osnovne podatke za novu momčad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showNameError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Naziv momčadi *") },
                    placeholder = {
                        Text("Npr. Seniori")
                    },
                    singleLine = true,
                    enabled = !isSaving,
                    isError = showNameError,
                    supportingText = if (showNameError) {
                        { Text("Naziv momčadi je obvezan.") }
                    } else null,
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Opis (nije obvezno)") },
                    placeholder = {
                        Text("Kratki opis momčadi...")
                    },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp)
                )

                Text(
                    text = "* Obvezno polje",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                saveError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            showNameError = true
                        } else {
                            onCreate(
                                CreateTeamRequest(
                                    name = name,
                                    description = description
                                ).validate()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Kreiraj momčad")
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Natrag")
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 850)
@Composable
private fun CreateTeamPreview() {
    SquadSyncTheme(darkTheme = false) {
        CreateTeamScreen(
            onBack = {},
            onCreate = {}
        )
    }
}
