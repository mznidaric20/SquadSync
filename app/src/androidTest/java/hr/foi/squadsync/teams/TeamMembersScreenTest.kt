package hr.foi.squadsync.teams

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import hr.foi.squadsync.R
import hr.foi.squadsync.data.local.LocalTeamJoinRepository
import hr.foi.squadsync.domain.auth.SessionState
import hr.foi.squadsync.domain.auth.UserRole
import hr.foi.squadsync.ui.auth.SquadSyncApp
import hr.foi.squadsync.ui.teams.TeamsRoute
import hr.foi.squadsync.ui.teams.TeamsScreen
import hr.foi.squadsync.ui.teams.TeamsUiState
import hr.foi.squadsync.ui.theme.SquadSyncTheme
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeamMembersScreenTest {
    @get:Rule val compose = createComposeRule()
    private val scenario = TeamStoryScenario()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun athleteDoesNotSeeCsvExportButton() {
        showExportScreen(UserRole.ATHLETE)
        compose.onNodeWithTag("selected_team_name").assertExists()
        compose.onNodeWithTag("teams_list").performScrollToIndex(3)
        compose.onNodeWithTag("export_csv").assertDoesNotExist()
    }

    @Test fun coachSeesCsvExportButton() {
        showExportScreen(UserRole.COACH)
        compose.onNodeWithTag("teams_list").performScrollToIndex(3)
        compose.onNodeWithTag("export_csv").performScrollTo().assertIsDisplayed().assertIsEnabled()
    }

    private fun showExportScreen(role: UserRole) {
        compose.setContent {
            SquadSyncTheme(darkTheme = false) {
                TeamsScreen(
                    state = TeamsUiState(
                        teams = listOf(scenario.first), selectedTeamId = scenario.first.id,
                        members = listOf(scenario.ana, scenario.ivan)
                    ),
                    onBack = {}, userRole = role
                )
            }
        }
    }

    @Test fun coachOpensMembersFromTheTeamCardAndReturnsToTheTeamList() {
        showApp()
        compose.onNodeWithTag("open_teams").performScrollTo().performClick()
        waitFor("show_members_${scenario.first.id}")
        assertEquals(0, scenario.source.memberCalls)
        capture("05-my-teams.png")
        compose.onNodeWithTag("show_members_${scenario.first.id}").performScrollTo().performClick()
        waitFor("member_${scenario.ana.id}")
        compose.onNodeWithTag("teams_back").performClick()
        waitFor("show_members_${scenario.first.id}")
        compose.onNodeWithTag("my_teams").assertIsDisplayed()
        compose.onNodeWithTag("team_selector").assertDoesNotExist()
        compose.onNodeWithTag("member_${scenario.ana.id}").assertDoesNotExist()
    }

    @Test fun selectingOwnedTeamsShowsOnlyTheSelectedTeamsMembers() {
        showApp()
        compose.onNodeWithTag("open_teams").performScrollTo().performClick()
        waitFor("show_members_${scenario.first.id}")
        compose.onNodeWithTag("show_members_${scenario.first.id}").performScrollTo().performClick()
        waitFor("member_${scenario.ana.id}")
        compose.onNodeWithText(scenario.ana.fullName).assertIsDisplayed()
        compose.onNodeWithText(scenario.ana.email).assertIsDisplayed()
        compose.onNodeWithText(scenario.ivan.fullName).assertExists()
        capture("01-members.png")

        compose.onNodeWithTag("team_selector").assertDoesNotExist()
        compose.onNodeWithTag("teams_back").performClick()
        waitFor("show_members_${scenario.second.id}")
        compose.onNodeWithTag("show_members_${scenario.foreign.id}").assertDoesNotExist()
        compose.onNodeWithTag("show_members_${scenario.second.id}").performScrollTo().performClick()
        waitFor("member_${scenario.petar.id}")
        compose.onNodeWithText(scenario.petar.fullName).assertIsDisplayed()
        compose.onNodeWithText(scenario.petar.email).assertIsDisplayed()
        compose.onNodeWithTag("member_${scenario.ana.id}").assertDoesNotExist()
        compose.onNodeWithTag("member_${scenario.ivan.id}").assertDoesNotExist()
        compose.onNodeWithTag("member_${scenario.privateMember.id}").assertDoesNotExist()
    }

    @Test fun emptyTeamShowsTheExplicitEmptyRosterMessage() {
        showTeams(scenario.empty.id)
        waitFor("empty_team")
        compose.onNodeWithTag("empty_team").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.teams_empty_title)).assertIsDisplayed()
        compose.onNodeWithTag("member_${scenario.ana.id}").assertDoesNotExist()
        compose.onNodeWithTag("no_teams").assertDoesNotExist()
        capture("02-empty-roster.png")
    }

    @Test fun coachWithoutTeamsGetsTheSeparateNoTeamsMessage() {
        scenario.auth.signInAs(scenario.coachWithoutTeams)
        showApp()
        compose.onNodeWithTag("open_teams").performScrollTo().performClick()
        waitFor("no_teams")
        compose.onNodeWithTag("no_teams").assertIsDisplayed()
        compose.onNodeWithText("Još nemaš kreiranih momčadi.").assertIsDisplayed()
        compose.onNodeWithTag("team_selector").assertDoesNotExist()
        compose.onNodeWithTag("empty_team").assertDoesNotExist()
        assertEquals(0, scenario.source.memberCalls)
        capture("03-no-teams.png")
    }

    @Test fun athleteCannotOpenTheCoachMemberList() {
        scenario.auth.signInAs(scenario.athleteUser)
        showApp()
        compose.onNodeWithTag("session_greeting").assertIsDisplayed()
        compose.onNodeWithTag("open_teams").assertDoesNotExist()
        compose.onNodeWithTag("export_csv").assertDoesNotExist()
        compose.onNodeWithTag("team_selector").assertDoesNotExist()
        assertEquals(0, scenario.source.listCalls)
    }

    @Test fun deniedOwnershipShowsAnErrorWithoutReadingMembers() {
        scenario.source.denyFirstRoster = true
        showTeams()
        waitFor("teams_error")
        compose.onNodeWithText(context.getString(R.string.teams_access_denied)).assertIsDisplayed()
        compose.onNodeWithTag("member_${scenario.ana.id}").assertDoesNotExist()
        compose.onNodeWithTag("member_${scenario.privateMember.id}").assertDoesNotExist()
        compose.onNodeWithTag("empty_team").assertDoesNotExist()
        assertEquals(0, scenario.source.memberCalls)
        capture("04-access-denied.png")
    }

    @Test fun failedFetchShowsAnErrorAndRetryLoadsMembers() {
        scenario.source.failNextMembers = true
        showTeams()
        waitFor("teams_error")
        compose.onNodeWithText(context.getString(R.string.teams_data_unavailable)).assertIsDisplayed()
        compose.onNodeWithTag("no_teams").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.retry)).performScrollTo().performClick()
        waitFor("member_${scenario.ana.id}")
        compose.onNodeWithTag("teams_error").assertDoesNotExist()
        assertEquals(2, scenario.source.memberCalls)
    }

    @Test fun pendingFetchShowsLoadingInsteadOfAnEmptyTeam() {
        val gate = CompletableDeferred<Unit>()
        scenario.source.memberGate = gate
        showTeams()
        waitFor("teams_loading")
        compose.onNodeWithText(context.getString(R.string.teams_loading)).assertIsDisplayed()
        compose.onNodeWithTag("no_teams").assertDoesNotExist()
        compose.onNodeWithTag("empty_team").assertDoesNotExist()
        compose.runOnIdle { gate.complete(Unit) }
        waitFor("member_${scenario.ana.id}")
        compose.onNodeWithTag("teams_loading").assertDoesNotExist()
    }

    @Test fun logoutDuringMemberFetchShowsLoginAndNeverShowsThePendingRoster() {
        val gate = CompletableDeferred<Unit>()
        scenario.source.memberGate = gate
        showApp()
        compose.onNodeWithTag("open_teams").performScrollTo().performClick()
        waitFor("show_members_${scenario.first.id}")
        compose.onNodeWithTag("show_members_${scenario.first.id}").performScrollTo().performClick()
        waitFor("teams_loading")
        compose.runOnIdle { scenario.auth.sessionState.value = SessionState.SignedOut }
        compose.runOnIdle { gate.complete(Unit) }
        compose.onNodeWithTag("login_email").assertExists()
        compose.onNodeWithTag("teams_list").assertDoesNotExist()
        compose.onNodeWithTag("member_${scenario.ana.id}").assertDoesNotExist()
    }

    @Test fun roleOverviewOffersMembersThroughOwnTeamsAndSystemBackRestoresTheList() {
        showApp()
        compose.onNodeWithText(context.getString(R.string.access_tab_role)).performClick()
        compose.onNodeWithText("Pregled članova momčadi").assertDoesNotExist()
        capture("06-role-overview.png")
        compose.onNodeWithText(context.getString(R.string.access_view_teams)).performScrollTo().performClick()
        waitFor("show_members_${scenario.first.id}")
        compose.onNodeWithTag("show_members_${scenario.first.id}").performScrollTo().performClick()
        waitFor("member_${scenario.ana.id}")
        pressBack()
        waitFor("show_members_${scenario.first.id}")
        compose.onNodeWithTag("my_teams").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText(context.getString(R.string.access_role_coach)).assertIsDisplayed()
    }

    private fun showTeams(teamId: String = scenario.first.id) {
        val actor = (scenario.auth.sessionState.value as SessionState.Authenticated).session.user
        compose.setContent {
            SquadSyncTheme(darkTheme = false) {
                TeamsRoute(coachId = actor.id, teamId = teamId, repository = scenario.repository, onBack = {})
            }
        }
    }

    private fun showApp() {
        compose.setContent {
            SquadSyncTheme(darkTheme = false) {
                SquadSyncApp(
                    repository = scenario.auth,
                    teamJoinRepository = LocalTeamJoinRepository(),
                    teamRepository = scenario.repository
                )
            }
        }
    }

    private fun waitFor(tag: String) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun capture(name: String) {
        val arguments = InstrumentationRegistry.getArguments()
        val directory = arguments.getString("additionalTestOutputDir")?.let(::File)
            ?: File(requireNotNull(context.getExternalFilesDir(null)), "team-story-evidence")
        check(directory.exists() || directory.mkdirs())
        File(directory, name).outputStream().use { output ->
            check(compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}
