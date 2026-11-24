package hr.foi.squadsync.ui.access

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.access.AccessAction
import hr.foi.squadsync.domain.access.RoleAccessPolicy
import hr.foi.squadsync.domain.auth.UserProfile
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.auth.AuthIcon
import hr.foi.squadsync.ui.auth.SessionScreen
import hr.foi.squadsync.ui.auth.SessionUiState
import androidx.compose.foundation.clickable

private enum class RoleTab { PROFILE, ROLE_OVERVIEW }

private data class AccessEntry(
    val action: AccessAction,
    val title: Int,
    val explanation: Int
)

// Prikazujemo samo mogucnosti koje odgovaraju ulozi iz prijavljene sesije.
// Ekrani za upravljanje momcadima nisu dio SCRUM-42; spajaju se kroz druge zadatke.
private val accessEntries = listOf(
    AccessEntry(AccessAction.CREATE_TEAM, R.string.access_create_team, R.string.access_create_team_info),
    AccessEntry(AccessAction.VIEW_OWN_TEAMS, R.string.access_view_teams, R.string.access_view_teams_info),
    AccessEntry(AccessAction.VIEW_MEMBERS_OF_OWN_TEAMS, R.string.access_view_members, R.string.access_view_members_info),
    AccessEntry(AccessAction.JOIN_TEAM, R.string.access_join_team, R.string.access_join_team_info),
    AccessEntry(AccessAction.VIEW_OWN_MEMBERSHIPS, R.string.access_view_memberships, R.string.access_view_memberships_info)
)

@Composable
fun RoleAwareScreen(
    user: UserProfile,
    state: SessionUiState,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onCreateTeamClick: () -> Unit = {},
    onMyTeamsClick: () -> Unit = {}
)
 {
    var tab by rememberSaveable(user.id) { mutableStateOf(RoleTab.PROFILE) }
    BackHandler(enabled = tab == RoleTab.ROLE_OVERVIEW) { tab = RoleTab.PROFILE }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == RoleTab.PROFILE,
                    onClick = { tab = RoleTab.PROFILE },
                    icon = { AuthIcon(R.drawable.ic_person) },
                    label = { Text(stringResource(R.string.access_tab_profile)) }
                )
                NavigationBarItem(
                    selected = tab == RoleTab.ROLE_OVERVIEW,
                    onClick = { tab = RoleTab.ROLE_OVERVIEW },
                    icon = { AuthIcon(if (user.role == UserRole.COACH) R.drawable.ic_coach else R.drawable.ic_athlete) },
                    label = { Text(stringResource(R.string.access_tab_role)) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {

                RoleTab.PROFILE -> SessionScreen(
                    user = user,
                    state = state,
                    onRefresh = onRefresh,
                    onSignOut = onSignOut,
                    onCreateTeamClick = onCreateTeamClick,
                    onMyTeamsClick = onMyTeamsClick
                )
                RoleTab.ROLE_OVERVIEW -> RoleOverviewScreen(
                    user = user,
                    onCreateTeamClick = onCreateTeamClick,
                    onMyTeamsClick = onMyTeamsClick
                )
            }
        }
    }
}

@Composable
private fun RoleOverviewScreen(user: UserProfile, onCreateTeamClick: () -> Unit, onMyTeamsClick: () -> Unit) {
    val title = if (user.role == UserRole.COACH) R.string.access_role_coach else R.string.access_role_athlete
    val entries = accessEntries.filter { RoleAccessPolicy.canAccess(user.role, it.action) }

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(PaddingValues(horizontal = 20.dp, vertical = 24.dp)),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.access_role_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        entries.forEach { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = item.action == AccessAction.CREATE_TEAM ||
                                item.action == AccessAction.VIEW_OWN_TEAMS
                    ) {
                        when (item.action) {
                            AccessAction.CREATE_TEAM -> onCreateTeamClick()
                            AccessAction.VIEW_OWN_TEAMS -> onMyTeamsClick()
                            else -> Unit
                        }
                    },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    AuthIcon(R.drawable.ic_shield, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(item.title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(item.explanation),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Text(
            stringResource(R.string.access_role_development_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(R.string.access_role_security_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
