package hr.foi.squadsync.ui.teams

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hr.foi.squadsync.domain.teams.TeamJoinRepository

fun teamViewModelFactory(repository: TeamJoinRepository) = viewModelFactory {
    initializer { JoinTeamViewModel(repository) }
}
