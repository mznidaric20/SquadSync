package hr.foi.squadsync.access

import hr.foi.squadsync.domain.access.AccessAction
import hr.foi.squadsync.domain.access.RoleAccessPolicy
import hr.foi.squadsync.domain.auth.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleAccessPolicyTest {

    @Test fun coachCanExportCsv() {
        assertTrue(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.EXPORT_TEAM_CSV))
    }

    @Test fun athleteCannotExportCsv() {
        assertFalse(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.EXPORT_TEAM_CSV))
    }

    @Test fun coachCanOnlyUseCoachActions() {
        assertTrue(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.VIEW_OWN_PROFILE))
        assertTrue(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.CREATE_TEAM))
        assertTrue(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.VIEW_OWN_TEAMS))
        assertTrue(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.VIEW_MEMBERS_OF_OWN_TEAMS))
        assertFalse(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.JOIN_TEAM))
        assertFalse(RoleAccessPolicy.canAccess(UserRole.COACH, AccessAction.VIEW_OWN_MEMBERSHIPS))
    }

    @Test fun athleteCanOnlyUseAthleteActions() {
        assertTrue(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.VIEW_OWN_PROFILE))
        assertTrue(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.JOIN_TEAM))
        assertTrue(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.VIEW_OWN_MEMBERSHIPS))
        assertFalse(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.CREATE_TEAM))
        assertFalse(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.VIEW_OWN_TEAMS))
        assertFalse(RoleAccessPolicy.canAccess(UserRole.ATHLETE, AccessAction.VIEW_MEMBERS_OF_OWN_TEAMS))
    }

    @Test(expected = SecurityException::class)
    fun athleteCannotPerformCoachAction() {
        RoleAccessPolicy.requireAccess(UserRole.ATHLETE, AccessAction.CREATE_TEAM)
    }

    @Test(expected = SecurityException::class)
    fun coachCannotPerformAthleteAction() {
        RoleAccessPolicy.requireAccess(UserRole.COACH, AccessAction.JOIN_TEAM)
    }
}
