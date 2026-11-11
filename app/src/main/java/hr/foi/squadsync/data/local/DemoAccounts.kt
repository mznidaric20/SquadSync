package hr.foi.squadsync.data.local

import hr.foi.squadsync.domain.auth.RegistrationRequest
import hr.foi.squadsync.domain.auth.UserRole

/** Public development credentials; these seed the same list used by registration and login. */
object DemoAccounts {
    val users: List<RegistrationRequest> = listOf(
        RegistrationRequest(
            firstName = "Luka",
            lastName = "Horvat",
            email = "sportas@squadsync.hr",
            password = "Sportas123!",
            role = UserRole.ATHLETE
        ),
        RegistrationRequest(
            firstName = "Marko",
            lastName = "Kovač",
            email = "trener@squadsync.hr",
            password = "Trener123!",
            role = UserRole.COACH
        )
    )
}
