package hr.foi.squadsync.auth

import hr.foi.squadsync.data.local.PasswordHasher
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test fun onlyTheCorrectPasswordMatches() {
        val salt = hasher.newSalt()
        val hash = hasher.hash("Lozinka123", salt)
        assertNotEquals("Lozinka123", hash)
        assertTrue(hasher.matches("Lozinka123", salt, hash))
        assertFalse(hasher.matches("DrugaLozinka123", salt, hash))
    }

    @Test fun identicalPasswordsUseDifferentSaltsAndHashes() {
        val firstSalt = hasher.newSalt()
        val secondSalt = hasher.newSalt()
        assertNotEquals(firstSalt, secondSalt)
        assertNotEquals(hasher.hash("Lozinka123", firstSalt), hasher.hash("Lozinka123", secondSalt))
    }
}
