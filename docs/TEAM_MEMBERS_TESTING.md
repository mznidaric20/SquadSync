# Testiranje pregleda članova momčadi

Datum provjere: **23. 11. 2026.**

Priča: **Kao trener želim moći pregledavati članove svoje momčadi kako bih imao uvid u njezin sastav.**

## Što je pronađeno u projektu i granama

Provjereni su lokalni kod i udaljene grane nakon `git fetch origin`. Supabase je uveden za autentifikaciju i profil korisnika. Pregled momčadi i članova zasad koristi lokalni izvor podataka i nalazi se na zasebnoj grani.

| Grana / commit pri provjeri | Pronađeno stanje |
| --- | --- |
| `origin/main` / `6be3c47` | Starija početna verzija projekta. |
| `origin/dev` / `8ee6285` | Spojena Supabase prijava, registracija i dohvat profila iz tablice `korisnici`; nema team repozitorija ni ekrana članova. |
| `origin/feature/SCRUM-30-supabase-registration` / `6096445` | `SupabaseAuthDataSource`, provider, DTO i mapper za korisnika. |
| `origin/feature/pregled-clanova` / `dc01f83` | Ekran članova, odabir momčadi, prazna stanja, MVVM, lokalni dohvat i provjera ovlasti. |
| `origin/feature/SCRUM-17-create-team` / `b841c15` | Ekran kreiranja, navigacija, modeli, validacija i ugovor repozitorija; nema implementacije spremanja momčadi u Supabase. |
| `origin/feature/SCRUM-19-add-athletes-to-team` / `d79630f` | Validator pristupnog koda i njegov test; nema dohvata članova iz baze. |
| `origin/feature/SCRUM-42-role-access` / `1e5bce4` | Pravila pristupa po ulozi, UI i testovi. Komentar o RLS-u nije dokaz da postoje politike za momčadi. |
| `origin/dev-backend` / `772d2be` | Samo početna struktura: `Backend/.gitignore` i README. |

U pregledanim granama nisu pronađeni Supabase izvor za momčadi/članove, njihove SQL migracije ni RLS politike. Stvarno stanje udaljene baze nije provjereno; kod u repozitoriju ne daje dokaz da je ta integracija napravljena. Lokalno također nisu postavljeni Supabase URL i ključ.

Na `origin/dev` postoji i `SupabaseMapperTest` s devet jediničnih provjera mapiranja uloga i pogrešaka. To nisu testovi dohvata članova ni poslužiteljskih ovlasti; nisu uključeni u dolje navedene rezultate ove grane.

Testovi pripadaju grani **`feature/pregled-clanova`** i provjeravaju postojeći pregled članova iz commita `dc01f83`, uz dodane testove. Supabase auth grana nije spojena u ovu granu.

## Opseg testiranja

Jedinični testovi koriste kontrolirane sesije i izvore podataka. UI testovi na emulatoru pokreću stvarni `SquadSyncApp` / `TeamsRoute`, ViewModel i `DefaultTeamRepository`, uz izolirani testni auth i izvor podataka. Izvor namjerno vraća i tuđu momčad kako bi se provjerilo filtriranje u repozitoriju.

Integracijski testovi pohrane na Androidu koriste stvarne `EncryptedTeamStore`, `EncryptedFileStore`, `LocalTeamDataSource` i `DefaultTeamRepository`. Svaki test dobiva vlastite datoteke s UUID prefiksom te ih nakon testa uklanja. Ne mijenjaju se stvarni računi i momčadi; nema registriranja korisnika ili upisivanja podataka u Supabase.

## Acceptance criteria

| Kriterij | Automatska provjera | Rezultat |
| --- | --- | --- |
| 1. Trener može odabrati momčad koju vodi. | Repozitorij vraća samo vlastite momčadi; UI otvara odabir i prelazi na drugu vlastitu momčad. | Prolazi: jedinični i UI testovi, lokalni/testni izvor. |
| 2. Prikazuju se sportaši odabrane momčadi. | Uspoređuju se očekivani članovi za dvije momčadi; stari članovi nestaju pri promjeni, a kasni odgovor otkazanog zahtjeva ne mijenja odabir. | Prolazi: jedinični i UI testovi, lokalni/testni izvor. |
| 3. Vide se osnovni identifikacijski podaci. | UI provjerava ime, prezime i e-poštu; integracijski test provjerava očuvanje identiteta i podataka nakon šifriranog zapisa i čitanja. | Prolazi: UI i šifrirana pohrana. |
| 4. Prazna momčad ima odgovarajuću poruku. | Testira se prazni roster, obnova odabira prazne momčadi te odvojeni prikaz za trenera bez ijedne momčadi. | Prolazi: jedinični testovi, UI i pohrana. |
| 5. Trener ne može pristupiti tuđoj momčadi. | Izravan poziv poznatog tuđeg ID-a se odbija prije čitanja članova; provjeravaju se drugi trener, sportaš, nevaljana sesija, promjena vlasništva i odjava tijekom dohvata. | Prolazi lokalno: repozitorij, UI i pohrana. Supabase/RLS nije testiran. |

Pokriveni su i nepoznati ID momčadi, neusklađeni metapodaci, pogreška dohvata s ponovnim pokušajem, stanje učitavanja, oštećena pohrana te brisanje prethodno prikazanih podataka nakon pogreške ovlasti.

## Testovi i rezultati

| Testna klasa | Broj testova | Svrha |
| --- | ---: | --- |
| `TeamRepositoryTest` | 19 | Provjera sesije, filtriranje momčadi, vlasništvo, dohvat odgovarajućeg rostera i promjene tijekom zahtjeva. |
| `LocalTeamDataSourceTest` | 5 | Trajno vlasništvo lokalnog kataloga, lokalna provjera pristupa i ponašanje praznog kataloga. |
| `TeamsViewModelTest` | 7 | Odabir i obnova momčadi, otkazivanje zahtjeva, prazna stanja i uklanjanje zastarjelih podataka. |
| `TeamMembersScreenTest` | 9 | Navigacija, odabir i podaci članova, prazna stanja, ovlasti, greška i ponovni pokušaj, učitavanje i odjava. |
| `TeamStorageIntegrationTest` | 5 | Stvarno šifrirano spremanje i ponovno čitanje na Androidu, drugi trener, promjena vlasnika i oštećena datoteka. |

Za ovu priču postoje **31 jedinični i 14 Android testova**. U ovoj izmjeni dodano je devet jediničnih i 14 Android testova; ostali su postojali uz prethodna dva subtaska.

- Jedinični testovi cijelog projekta: **69/69 prolazi**, uključujući svih 31 za ovu priču.
- Android testovi cijelog projekta: **18/18 prolazi**, uključujući devet UI i pet integracijskih testova za ovu priču. Ostala četiri su postojeće provjere konteksta aplikacije i lokalne auth sesije. Nema preskočenih testova.
- Android Lint: **prolazi, bez pogrešaka**, uz 18 upozorenja o verzijama alata/ovisnosti, neiskorištenim resursima i suvišnoj oznaci aplikacije. Nema upozorenja u dodanim testovima.

Okruženje: Windows, Android Studio JBR, AVD `Medium_Phone_API_37.0`, Android 17 / API 37. Emulator je pokrenut kao zasebna read-only instanca na portu 5580, bez spremanja promjena u izvorni AVD.

Prvi pokušaj UI izvođenja otkrio je nekompatibilnost stare verzije Espressa s API-jem 37 (`InputManager.getInstance`). Testne ovisnosti ažurirane su na Espresso **3.7.0** i AndroidX JUnit **1.3.0**; ispravak pristupa `InputManager` naveden je u [službenim bilješkama AndroidX Testa](https://developer.android.com/jetpack/androidx/releases/test). Time se mijenjaju testne ovisnosti, bez promjene produkcijske logike aplikacije.

## Kako ponoviti u Android Studiju

1. Odaberi granu `feature/pregled-clanova` i napravi Gradle Sync.
2. Za jedinične testove otvori `app/src/test/java/hr/foi/squadsync/teams` i pokreni testove tog paketa.
3. Pokreni emulator. U `app/src/androidTest/java/hr/foi/squadsync/teams` pokreni `TeamMembersScreenTest` i `TeamStorageIntegrationTest`.
4. U prozoru Run provjeri da su sve metode zelene. Za cijeli projekt možeš koristiti naredbe ispod.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
# Emulator treba biti pokrenut; ako ih ima više, postavi ANDROID_SERIAL.
.\gradlew.bat :app:connectedDebugAndroidTest
```

Gradle izvještaji nalaze se u `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/androidTests/connected/debug/index.html` i `app/build/reports/lint-results-debug.html`. Generirani `build` sadržaj nije namijenjen commitu.

Četiri snimke iz uspješnog UI izvođenja nalaze se u `app/build/reports/team-story-evidence`: `01-members.png`, `02-empty-roster.png`, `03-no-teams.png` i `04-access-denied.png`. Vizualno su provjereni čitljivi podaci članova, obje prazne poruke i prikaz odbijenog pristupa bez članova. Snimke koriste isključivo izmišljene testne identitete. Gradle ih izvorno prikuplja pod `app/build/outputs/connected_android_test_additional_output`.

Za ručnu lokalnu demonstraciju prijavi se kao `trener@squadsync.hr` / `Trener123!`, otvori **Moje momčadi**, usporedi Seniore i Juniore te provjeri poruku prazne momčadi. Novi lokalno registrirani trener nema dodijeljenih momčadi pa mora dobiti zasebnu poruku za prazan katalog. Ti ogledni računi i podaci pripadaju lokalnoj implementaciji.

## Što treba provjeriti nakon povezivanja momčadi na Supabase

Ovaj dio **nije izvršen**. Za njegovo testiranje trebaju postojati stvarna implementacija `TeamDataSource` za Supabase, dogovorena shema članstva/vlasništva, politike pristupa i testno okruženje s korisničkim računima.

| Scenarij na testnoj bazi | Očekivanje |
| --- | --- |
| Trener A ima dvije momčadi s različitim članovima i jednu praznu. | A vidi samo svoje momčadi; izbor vraća pripadajuće članove; prazna vraća uspješan prazan popis. |
| Trener B ima vlastitu momčad i članove. | B ne vidi A-ove momčadi i obratno. |
| A pošalje izravan zahtjev s poznatim ID-em B-ove momčadi. | Poslužitelj odbija zahtjev ili vraća nula dopuštenih redaka; članovi B-a ne izlaze iz baze. |
| Sportaš ili neprijavljeni korisnik pošalje isti zahtjev. | Nema pristupa trenerskom popisu članova mimo dopuštenog poslovnog pravila. |
| Ukloni se ovlast trenera, istekne/poništi sesija ili se promijeni korisnik. | Sljedeći dohvat nema pristup starim podacima; aplikacija ne prikazuje prethodni roster. |
| Dohvat vrati mrežnu grešku. | Prikazuje se pogreška i mogućnost ponovnog pokušaja, a ne poruka da je momčad prazna. |

Poslužiteljske scenarije treba izvršiti tokenima običnih testnih korisnika. `service_role` pristup može zaobići RLS i zato nije odgovarajući dokaz ovih ovlasti; vidi [Supabase RLS dokumentaciju](https://supabase.com/docs/guides/database/postgres/row-level-security).

Pri spajanju grana treba posebno riješiti identitete: lokalne ogledne momčadi vezane su uz UUID računa iz lokalne pohrane. Supabase korisnik ima svoj `auth.uid()`. Sama zamjena prijave Supabaseom ne prenosi vlasništvo lokalnih momčadi i nije dovoljna da se one pojave.

**Status subtaska:** testiranje postojeće lokalne implementacije je završeno: svih 45 testova ove priče prolazi. Potvrda rada sa Supabase momčadima i serverom zahtijeva gore navedenu integraciju i zasebno izvršavanje testova nad testnom bazom. Za završetak priče koja zahtijeva stvarni backend treba odraditi i taj dio.

## Kratki tekst za subtask

Testiran je odabir vlastite momčadi, prikaz pripadajućih sportaša i njihovih identifikacijskih podataka, prazna momčad te zabrana pristupa tuđim momčadima. Izvršen je 31 jedinični i 14 Android testova pregleda članova; svi prolaze, uključujući provjere stvarne šifrirane pohrane i UI-a na emulatoru API 37. Udaljene grane pregledane su nakon fetcha: Supabase je povezan za autentifikaciju, dok pregled članova još koristi lokalni izvor. Testiranje poslužiteljskog dohvata i RLS-a ostaje nakon uvođenja te integracije.
