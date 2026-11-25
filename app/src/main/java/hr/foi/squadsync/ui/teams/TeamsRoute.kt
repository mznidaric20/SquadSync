package hr.foi.squadsync.ui.teams

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.R
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TeamsRoute(coachId: String, teamId: String, repository: TeamRepository, onBack: () -> Unit) {
    val factory = remember(repository, teamId) {
        viewModelFactory {
            initializer {
                TeamsViewModel(
                    repository = repository,
                    teamId = teamId
                )
            }
        }
    }
    val viewModel: TeamsViewModel = viewModel(key = "team_members_${coachId}_$teamId", factory = factory)
    LifecycleStartEffect(viewModel) {
        viewModel.refresh()
        onStopOrDispose { viewModel.onScreenClosed() }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var csvToSave by rememberSaveable { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val saveCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            val csv = csvToSave
            isSaving = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openOutputStream(uri)
                            ?: throw IOException("Nije moguće otvoriti datoteku.")
                        stream.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                    }
                    viewModel.onExportSaved()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    viewModel.onExportFailed()
                } finally {
                    csvToSave = ""
                    isSaving = false
                }
            }
        } else {
            csvToSave = ""
        }
    }

    val exportMessage = when (state.exportResult) {
        ExportResult.SUCCESS -> stringResource(R.string.csv_export_saved)
        ExportResult.FAILED -> stringResource(R.string.csv_export_failed)
        null -> null
    }
    LaunchedEffect(state.exportResult) {
        if (exportMessage == null) return@LaunchedEffect
        snackbarHostState.showSnackbar(exportMessage)
        viewModel.onExportMessageShown()
    }

    TeamsScreen(
        state = state, onBack = onBack, onReload = viewModel::refresh,
        snackbarHostState = snackbarHostState, isSaving = isSaving,
        onExport = {
            // Dijalog privremeno zatvara ekran, pa CSV pripremamo prije njegova otvaranja.
            csvToSave = viewModel.exportCsv()
            val teamName = state.selectedTeam?.name ?: "momcad"
            val fileName = "sportasi_${teamName.lowercase(Locale.ROOT).replace(" ", "_")}.csv"
            try {
                saveCsv.launch(fileName)
            } catch (exception: Exception) {
                csvToSave = ""
                viewModel.onExportFailed()
            }
        }
    )
}
