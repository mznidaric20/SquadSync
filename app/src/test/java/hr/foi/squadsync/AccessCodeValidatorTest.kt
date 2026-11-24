package hr.foi.squadsync

import hr.foi.squadsync.domain.validation.AccessCodeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessCodeValidatorTest {

    @Test
    fun givenValidCode_whenValidated_thenNoError() {
        assertNull(AccessCodeValidator.validate("X7K2P4"))
    }

    @Test
    fun givenEmptyCode_whenValidated_thenEmptyCodeError() {
        assertEquals("Unesite pristupni kod.", AccessCodeValidator.validate(""))
    }

    @Test
    fun givenOnlySpaces_whenValidated_thenEmptyCodeError() {
        assertEquals("Unesite pristupni kod.", AccessCodeValidator.validate("   "))
    }

    @Test
    fun givenTooShortCode_whenValidated_thenInvalidCodeError() {
        assertEquals("Pristupni kod nije ispravan.", AccessCodeValidator.validate("X7K2"))
    }

    @Test
    fun givenTooLongCode_whenValidated_thenInvalidCodeError() {
        assertEquals("Pristupni kod nije ispravan.", AccessCodeValidator.validate("X7K2P4A"))
    }

    @Test
    fun givenCodeWithLetterO_whenValidated_thenInvalidCodeError() {
        assertEquals("Pristupni kod nije ispravan.", AccessCodeValidator.validate("X7K2PO"))
    }

    @Test
    fun givenCodeWithSpecialCharacter_whenValidated_thenInvalidCodeError() {
        assertEquals("Pristupni kod nije ispravan.", AccessCodeValidator.validate("X7K2P#"))
    }

    @Test
    fun givenLowercaseCodeWithSpaces_whenNormalized_thenUppercaseWithoutSpaces() {
        assertEquals("X7K2P4", AccessCodeValidator.normalize(" x7k 2p4 "))
    }

    @Test
    fun givenLowercaseCodeWithSpaces_whenValidated_thenNoError() {
        assertNull(AccessCodeValidator.validate(" x7k 2p4 "))
    }
}
