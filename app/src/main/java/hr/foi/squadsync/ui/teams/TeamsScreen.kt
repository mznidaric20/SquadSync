package hr.foi.squadsync.ui.teams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.foi.squadsync.R
import hr.foi.squadsync.domain.teams.TeamMember
import hr.foi.squadsync.domain.teams.Team
import hr.foi.squadsync.domain.teams.TeamError
import hr.foi.squadsync.ui.auth.AuthIcon
import hr.foi.squadsync.ui.auth.CourtArtwork
import hr.foi.squadsync.ui.auth.SquadBrand
import hr.foi.squadsync.data.teams.local.DemoTeamCatalog
import hr.foi.squadsync.ui.theme.SquadCyan
import hr.foi.squadsync.ui.theme.SquadNavy
import hr.foi.squadsync.ui.theme.SquadSyncTheme

@Composable
fun TeamsScreen(
    state: TeamsUiState,
    onTeamSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onReload: () -> Unit = {}
) {
    val selectedTeam = state.selectedTeam
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TeamsTopBar(onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)
                .testTag("teams_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "intro") { TeamsIntro(isDemo = state.isDemo) }
            if (state.isLoading) {
                item(key = "loading_teams") {
                    TeamsLoadingState(stringResource(R.string.teams_loading),
                        Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(20.dp))
                }
            } else if (selectedTeam != null) {
                item(key = "selector") {
                    TeamSelector(state.teams, selectedTeam, onTeamSelect,
                        Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp))
                }
                item(key = "members_heading") {
                    RosterHeading(selectedTeam, onReload, !state.isLoadingMembers,
                        Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp))
                }
                if (state.isLoadingMembers) {
                    item(key = "loading_members") {
                        TeamsLoadingState(stringResource(R.string.teams_members_loading),
                            Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 20.dp))
                    }
                } else if (state.error != null) {
                    item(key = "roster_error") {
                        TeamsErrorState(state.error, onReload,
                            Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 20.dp))
                    }
                } else if (state.members.isEmpty()) {
                    item(key = "empty_roster_${selectedTeam.id}") {
                        TeamsEmptyState(
                            title = stringResource(R.string.teams_empty_title),
                            description = stringResource(R.string.teams_empty_description),
                            modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 20.dp)
                                .testTag("empty_team")
                        )
                    }
                } else {
                    items(state.members, key = { "${selectedTeam.id}_${it.id}" }) { member ->
                        MemberCard(member, Modifier.widthIn(max = 680.dp).fillMaxWidth()
                            .padding(horizontal = 20.dp).padding(bottom = 10.dp))
                    }
                }
            } else if (state.error != null) {
                item(key = "teams_error") {
                    TeamsErrorState(state.error, onReload,
                        Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(20.dp))
                }
            } else {
                item(key = "no_teams") {
                    TeamsEmptyState(
                        title = stringResource(R.string.teams_none_title),
                        description = stringResource(R.string.teams_none_description),
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(20.dp)
                            .testTag("no_teams")
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(SquadNavy).statusBarsPadding()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("teams_back")) {
            Icon(painterResource(R.drawable.ic_arrow), contentDescription = stringResource(R.string.teams_back),
                tint = Color.White, modifier = Modifier.size(23.dp).rotate(180f))
        }
        SquadBrand(compact = true)
        Spacer(Modifier.weight(1f))
        Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(50)) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AuthIcon(R.drawable.ic_coach, Modifier.size(17.dp), tint = SquadCyan)
                Text(stringResource(R.string.role_coach), color = Color.White,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TeamsIntro(isDemo: Boolean) {
    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(SquadNavy, Color(0xFF0E3670))))) {
        CourtArtwork(Modifier.matchParentSize())
        Column(Modifier.widthIn(max = 680.dp).fillMaxWidth().align(Alignment.Center)
            .padding(horizontal = 24.dp, vertical = 24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.teams_eyebrow), color = SquadCyan,
                    style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                if (isDemo) {
                    Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.teams_demo_badge), color = Color(0xFFBECEE6),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.teams_title), color = Color.White,
                style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.teams_description), color = Color(0xFFBECEE6),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.widthIn(max = 330.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamSelector(teams: List<Team>, selectedTeam: Team, onSelect: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Surface(modifier, shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 1.dp) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedTeam.name,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    label = { Text(stringResource(R.string.teams_select_label)) },
                    leadingIcon = { AuthIcon(R.drawable.ic_team, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .testTag("team_selector")
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    teams.forEach { team ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(team.name, style = MaterialTheme.typography.titleMedium)
                                    Text(stringResource(R.string.teams_members_count, team.memberCount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            leadingIcon = { AuthIcon(R.drawable.ic_team, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = if (team.id == selectedTeam.id) {{
                                AuthIcon(R.drawable.ic_check, tint = MaterialTheme.colorScheme.primary)
                            }} else null,
                            onClick = { expanded = false; onSelect(team.id) },
                            modifier = Modifier.testTag("select_${team.id}")
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(selectedTeam.sport, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(R.string.teams_members_count, selectedTeam.memberCount),
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun RosterHeading(team: Team, onReload: () -> Unit, enabled: Boolean, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(R.string.teams_members_heading), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.teams_roster_description, team.name), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onReload, enabled = enabled) {
            Icon(painterResource(R.drawable.ic_refresh), contentDescription = stringResource(R.string.teams_refresh),
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun MemberCard(member: TeamMember, modifier: Modifier) {
    Surface(modifier.semantics(mergeDescendants = true) {}.testTag("member_${member.id}"),
        shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(50.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center) {
                Text(member.initials, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(member.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(member.email, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.role_athlete), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp))
            }
            AuthIcon(R.drawable.ic_athlete, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TeamsLoadingState(message: String, modifier: Modifier) {
    Surface(modifier.testTag("teams_loading"), shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TeamsErrorState(error: TeamError, onRetry: () -> Unit, modifier: Modifier) {
    val description = when (error) {
        TeamError.SESSION_REQUIRED -> R.string.teams_session_required
        TeamError.ACCESS_DENIED -> R.string.teams_access_denied
        TeamError.NOT_FOUND -> R.string.teams_not_found
        TeamError.DATA_UNAVAILABLE -> R.string.teams_data_unavailable
    }
    Surface(modifier.testTag("teams_error"), shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuthIcon(R.drawable.ic_shield, tint = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.teams_error_title), style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center)
            Text(stringResource(description), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (error == TeamError.DATA_UNAVAILABLE || error == TeamError.NOT_FOUND) {
                Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun TeamsEmptyState(title: String, description: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_team), contentDescription = null, modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.widthIn(max = 330.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 920)
@Preview(showBackground = true, widthDp = 900, heightDp = 920)
@Composable
private fun TeamsPreview() {
    SquadSyncTheme(darkTheme = false) {
        TeamsScreen(sampleTeamState(), {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 840)
@Composable
private fun EmptyTeamPreview() {
    SquadSyncTheme(darkTheme = false) {
        TeamsScreen(sampleTeamState(index = 1), {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 920)
@Composable
private fun TeamsDarkPreview() {
    SquadSyncTheme(darkTheme = true) {
        TeamsScreen(sampleTeamState(), {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 840)
@Composable
private fun NoTeamsPreview() {
    SquadSyncTheme(darkTheme = false) { TeamsScreen(TeamsUiState(), {}, {}) }
}

private fun sampleTeamState(index: Int = 0): TeamsUiState {
    val rosters = DemoTeamCatalog.create("preview-coach")
    return TeamsUiState(teams = rosters.map { it.team }, selectedTeamId = rosters[index].team.id,
        members = rosters[index].members, isDemo = true)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 840)
@Composable
private fun TeamsLoadingPreview() {
    SquadSyncTheme(darkTheme = false) { TeamsScreen(TeamsUiState(isLoading = true), {}, {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 840)
@Composable
private fun TeamsDeniedPreview() {
    SquadSyncTheme(darkTheme = false) { TeamsScreen(TeamsUiState(error = TeamError.ACCESS_DENIED), {}, {}) }
}
