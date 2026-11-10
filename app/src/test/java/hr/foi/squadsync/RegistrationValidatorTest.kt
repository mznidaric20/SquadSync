package hr.foi.squadsync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationValidatorTest {

    private fun validacija(
        ime: String = "Marko",
        prezime: String = "Horvat",
        email: String = "marko@gmail.com",
        lozinka: String = "Test12345",
        potvrda: String = "Test12345",
        uloga: String = "Sportaš"
    ): RegistrationErrors {

        return RegistrationValidator.validate(
            ime,
            prezime,
            email,
            lozinka,
            potvrda,
            uloga
        )
    }

    @Test
    fun testIspravnihPodataka() {

        val rezultat = validacija()

        assertEquals(RegistrationErrors(), rezultat)
    }

    @Test
    fun testPraznogImena() {

        val rezultat = validacija(ime = "")

        assertEquals("Unesite ime.", rezultat.ime)
    }

    @Test
    fun testPraznogPrezimena() {

        val rezultat = validacija(prezime = "")

        assertEquals("Unesite prezime.", rezultat.prezime)
    }

    @Test
    fun testNeispravneEmailAdrese() {

        val rezultat = validacija(email = "marko123")

        assertEquals(
            "E-pošta nije ispravna.",
            rezultat.email
        )
    }

    @Test
    fun testPrekratkeLozinke() {

        val rezultat = validacija(
            lozinka = "123",
            potvrda = "123"
        )

        assertEquals(
            "Lozinka mora imati najmanje 8 znakova.",
            rezultat.lozinka
        )
    }

    @Test
    fun testRazlicitihLozinki() {

        val rezultat = validacija(
            lozinka = "Test12345",
            potvrda = "Test54321"
        )

        assertEquals(
            "Lozinke se ne podudaraju.",
            rezultat.potvrdaLozinke
        )
    }

    @Test
    fun testBezKorisnickeUloge() {

        val rezultat = validacija(uloga = "")

        assertEquals(
            "Odaberite korisničku ulogu.",
            rezultat.uloga
        )
    }

    @Test
    fun testUlogeTrener() {

        val rezultat = validacija(uloga = "Trener")

        assertNull(rezultat.uloga)
    }

    @Test
    fun testNepostojeceUloge() {

        val rezultat = validacija(uloga = "Administrator")

        assertEquals(
            "Odaberite dopuštenu korisničku ulogu.",
            rezultat.uloga
        )
    }
}