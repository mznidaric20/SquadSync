package hr.foi.squadsync

import hr.foi.squadsync.domain.teams.AccessCodeGenerator
import hr.foi.squadsync.domain.validation.AccessCodeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessCodeGeneratorTest {

    // Kod je nasumican, pa se provjerava vise kodova, a ne samo jedan.
    private val numberOfCodes = 100

    @Test
    fun givenGenerator_whenCodesGenerated_thenEachCodeHasSixCharacters() {
        for (i in 1..numberOfCodes) {
            val code = AccessCodeGenerator.generate()

            assertEquals(6, code.length)
        }
    }

    @Test
    fun givenGenerator_whenCodesGenerated_thenEachCodePassesValidator() {
        for (i in 1..numberOfCodes) {
            val code = AccessCodeGenerator.generate()

            assertNull(AccessCodeValidator.validate(code))
        }
    }
}
