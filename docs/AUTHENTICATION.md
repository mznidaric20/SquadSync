# Autentifikacija i sesija

SquadSync trenutačno koristi lokalnu autentifikaciju. Računi postoje samo u ovoj instalaciji aplikacije, bez komunikacije s poslužiteljem. Pri prvoj uporabi treba registrirati račun. Registracija i prijava otvaraju profil, a odjava briše spremljenu sesiju i zadržava račun za sljedeću prijavu.

## MVVM slojevi

| Sloj | Odgovornost |
| --- | --- |
| `ui/auth` | Compose ekrani bez pristupa pohrani, zajedničke komponente, `LoginViewModel`, `RegistrationViewModel` i `SessionViewModel`. ViewModeli izlažu nepromjenjivi `StateFlow`; ekrani šalju akcije preko callbackova. |
| `domain/auth` | Modeli korisnika i sesije, tipizirane uloge i pogreške, sučelje `AuthRepository` i stanja prijave. Bez Android ovisnosti. |
| `domain/validation` | Provjera e-pošte, obaveznih polja, lozinke, potvrde lozinke i uloge. |
| `data/auth` | `DefaultAuthRepository` upravlja životnim ciklusom sesije. `AuthDataSource` definira ugovor za autentifikaciju i dohvat profila, a `SessionStore` za trajnu pohranu sesije. |
| `data/local` | Lokalni adapter za račune, hashiranje lozinki, JSON mapiranje i šifrirana pohrana. |
| `di` | `AppContainer` povezuje jednu instancu repozitorija za cijelu aplikaciju. |

Smjer ovisnosti je `UI → ViewModel → AuthRepository ← DefaultAuthRepository → AuthDataSource / SessionStore`. UI ne poznaje lokalne datoteke ni budući HTTP klijent. Stanje se prikuplja pomoću `collectAsStateWithLifecycle`, prema [Androidovim preporukama za arhitekturu](https://developer.android.com/topic/architecture/recommendations).

## Životni ciklus sesije

1. Pri pokretanju prikazuje se stanje učitavanja dok se ne pročita spremljena sesija.
2. Ako sesija postoji i nije istekla, repozitorij predaje token izvoru podataka kroz `fetchCurrentUser` i dohvaća aktualni profil. Ažurirani profil ponovno se sprema u sesiju.
3. Registracija ili ispravna prijava izdaje nasumični lokalni token sa životnim vijekom od 30 dana. Sučelje prelazi na profil tek nakon uspješnog trajnog spremanja sesije.
4. Profil prikazuje ime, prezime, e-poštu i ulogu iz aktivne sesije. Akcija **Osvježi profil** ponovno dohvaća korisnika. Osvježavanje profila ne produljuje trajanje tokena.
5. Istekla, oštećena ili opozvana sesija uklanja se i ponovno se prikazuje prijava. `SessionViewModel` prati istek i dok aplikacija radi.
6. Privremena pogreška dohvata pri pokretanju zadržava spremljenu sesiju i omogućuje ponovni pokušaj. Korisnik može i obrisati spremljenu prijavu.
7. Odjava prvo uklanja lokalnu sesiju, zatim pokušava opozvati token u izvoru podataka. Neuspjeh udaljenog opoziva ne vraća lokalnu prijavu. Ako lokalno brisanje ne uspije, prikazuje se pogreška.

Zajednički repozitorij serijalizira izmjene sesije pomoću `Mutex`. Forme tijekom zahtjeva onemogućuju ponovno slanje i promjenu ekrana. Lozinke se ne zapisuju u spremljeno UI stanje i čiste se nakon prijave ili napuštanja forme.

## Lokalna pohrana

`auth_accounts.enc` sadrži korisničke profile, zasebnu nasumičnu sol i PBKDF2-HMAC-SHA256 hash lozinke (120 000 iteracija, 256 bita), hash aktivnog tokena i vrijeme isteka. `auth_session.enc` sadrži profil, token i istek. E-pošta se normalizira pri registraciji i prijavi.

Obje datoteke koriste AES-256-GCM s novim IV-om pri svakom pisanju i ključem iz [Android Keystorea](https://developer.android.com/privacy-and-security/keystore). Pišu se preko `AtomicFile` u `noBackupFilesDir`, pa se autentifikacijski podaci ne prenose Android backupom. Operacije pohrane i hashiranja rade na IO dispatcheru. Ne sprema se izvorna lozinka.

Lokalni adapter služi razvoju aplikacije. Poslužitelj pri integraciji treba biti odgovoran za provjeru lozinki, prava pristupa i izdavanje, osvježavanje i opoziv tokena.

## Zamjena backendom

1. Implementiraj `AuthDataSource` u novom `data/remote` paketu. Metode `signIn` i `register` trebaju mapirati odgovor API-ja u `AuthSession`, a `fetchCurrentUser` treba poslati `session.accessToken` API-ju i vratiti `UserProfile`. `signOut` opoziva token.
2. HTTP/transport DTO modele i njihovo mapiranje zadrži u podatkovnom sloju. API pogreške mapiraj u `AuthException`: neispravni podaci u `INVALID_CREDENTIALS`, zauzeta e-pošta u `EMAIL_ALREADY_USED`, opozvan token u `SESSION_EXPIRED`, a mrežne probleme u `SERVICE_UNAVAILABLE`.
3. U `AppContainer` zamijeni `LocalAuthDataSource` novim adapterom. Zadrži `DefaultAuthRepository` i `EncryptedSessionStore`; Compose ekrani i ViewModeli ostaju povezani s istim sučeljem. Dodaj Android dozvolu za internet kada se uvedu mrežni zahtjevi.
4. Ako API koristi refresh tokene, proširi model/pohranu i repozitorij mehanizmom obnove prije isteka. Trenutačna implementacija nakon isteka zahtijeva novu prijavu. Pri prijelazu s lokalnog adaptera ukloni lokalnu sesiju; lokalni računi ne migriraju se automatski na poslužitelj.

## Provjera

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

Jedinični testovi pokrivaju validaciju, normalizaciju podataka, trajno spremanje kroz ugovor pohrane, dohvat profila nakon ponovnog stvaranja repozitorija, istek/opoziv, mrežne i diskovne pogreške, odjavu, hashiranje i ponašanje ViewModela pri ponovnom slanju.

`SessionPersistenceTest` zahtijeva Android uređaj ili emulator. Provjerava stvarni Keystore i šifrirane datoteke, ponovnu prijavu, oštećenu sesiju, pogrešnu lozinku i duplikate računa. Za pokretanje na spojenom uređaju:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Compose previewi nalaze se uz ekrane prijave, registracije i profila. Za ručnu provjeru testiraj mali ekran, prikazanu tipkovnicu, povećan font, tamnu temu, promjenu orijentacije, odjavu i ponovno pokretanje procesa.
