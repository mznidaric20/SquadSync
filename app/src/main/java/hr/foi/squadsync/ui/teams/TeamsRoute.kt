package hr.foi.squadsync.ui.teams

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.domain.teams.TeamRepository
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.domain.access.AccessAction
import hr.foi.squadsync.domain.access.RoleAccessPolicy
import hr.foi.squadsync.data.export.saveCsvToDownloads
import hr.foi.squadsync.R
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TeamsRoute(
    coachId: String, teamId: String, repository: TeamRepository, onBack: () -> Unit,
    userRole: UserRole = UserRole.ATHLETE
) {
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
    var csvToSave by rememberSaveable(coachId, teamId) { mutableStateOf("") }
    var fileNameToSave by rememberSaveable(coachId, teamId) { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    fun saveExport() {
        if (isSaving) return
        val csv = csvToSave
        val fileName = fileNameToSave
        isSaving = true
        scope.launch {
            try {
                RoleAccessPolicy.requireAccess(userRole, AccessAction.EXPORT_TEAM_CSV)
                withContext(Dispatchers.IO) {
                    saveCsvToDownloads(context, fileName, csv)
                }
                viewModel.onExportSaved()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                viewModel.onExportFailed()
            } finally {
                csvToSave = ""
                fileNameToSave = ""
                isSaving = false
            }
        }
    }

    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            saveExport()
        } else {
            csvToSave = ""
            fileNameToSave = ""
            viewModel.onExportFailed()
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
        snackbarHostState = snackbarHostState, isSaving = isSaving, userRole = userRole,
        onExport = {
            if (!RoleAccessPolicy.canAccess(userRole, AccessAction.EXPORT_TEAM_CSV)) {
                viewModel.onExportFailed()
            } else if (!isSaving) {
                csvToSave = viewModel.exportCsv()
                val teamName = state.selectedTeam?.name ?: "momcad"
                val safeName = teamName.lowercase(Locale.ROOT)
                    .replace(" ", "_").replace("/", "_").replace("\\", "_")
                fileNameToSave = "sportasi_$safeName.csv"
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    saveExport()
                }
            }
        }
    )
}
