package hr.foi.squadsync.domain.access

import hr.foi.squadsync.domain.auth.UserRole

// Ovlasti su vezane uz ulogu, ali vlasnistvo nad podacima provjerava Supabase RLS.
enum class AccessAction {
    VIEW_OWN_PROFILE,
    CREATE_TEAM,
    VIEW_OWN_TEAMS,
    VIEW_MEMBERS_OF_OWN_TEAMS,
    JOIN_TEAM,
    VIEW_OWN_MEMBERSHIPS,
    EXPORT_TEAM_CSV
}

object RoleAccessPolicy {
    private val coachActions = setOf(
        AccessAction.VIEW_OWN_PROFILE,
        AccessAction.CREATE_TEAM,
        AccessAction.VIEW_OWN_TEAMS,
        AccessAction.VIEW_MEMBERS_OF_OWN_TEAMS,
        AccessAction.EXPORT_TEAM_CSV
    )

    private val athleteActions = setOf(
        AccessAction.VIEW_OWN_PROFILE,
        AccessAction.JOIN_TEAM,
        AccessAction.VIEW_OWN_MEMBERSHIPS
    )

    fun canAccess(role: UserRole, action: AccessAction): Boolean {
        return when (role) {
            UserRole.COACH -> action in coachActions
            UserRole.ATHLETE -> action in athleteActions
        }
    }

    fun requireAccess(role: UserRole, action: AccessAction) {
        if (!canAccess(role, action)) {
            throw SecurityException("User role is not allowed to perform this action")
        }
    }
}
