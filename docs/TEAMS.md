# Momčadi i članovi – navigacija i dohvat uz ovlasti

Pregled članova otvara se iz kartice pojedine momčadi na ekranu **Moje momčadi**. Popis momčadi i članovi koriste isti repozitorij: Supabase kada je konfiguriran, odnosno lokalni katalog u lokalnom načinu rada.

## Otvaranje ekrana

Prijavi se kao trener i na profilu odaberi **Moje momčadi**, ili na kartici **Moja uloga** odaberi **Pregled vlastitih momčadi**. Svaka kartica momčadi sadrži gumb **Prikaži članove**. Gumb otvara zaseban ekran samo za tu momčad, bez dodatnog izbornika momčadi. Strelica u zaglavlju i sistemska tipka Natrag vraćaju na **Moje momčadi**; sljedeći povratak vraća na prethodnu karticu profila/uloge.

Stavka **Pregled članova momčadi** uklonjena je s popisa funkcionalnosti trenera. Ovlast pregleda vlastitih članova ostaje u `RoleAccessPolicy`.

U lokalnom načinu rada testni trener (`trener@squadsync.hr` / `Trener123!`) dobiva dvije ogledne momčadi:

- **Seniori** – četiri sportaša s imenom, prezimenom, e-poštom i oznakom uloge.
- **Juniori** – prazna momčad koja prikazuje poruku da još nema članova.

Pri prvom dohvatu momčadi se povezuju sa stvarnim ID-em već spremljenog računa `trener@squadsync.hr`. Katalog se sprema u `teams.enc` preko postojeće AES-GCM pohrane u `noBackupFilesDir`. Postojeći katalog se ne prepisuje niti se vlasništvo mijenja prema korisniku koji trenutno traži podatke. Ogledni sportaši nisu povezani s registriranim računima.

Novi lokalno registrirani trener dobiva prazan popis jer mu još nije dodijeljena momčad. Prazna momčad i trener bez momčadi imaju odvojene poruke. Ekran također prikazuje učitavanje, pogrešku i ponovni pokušaj. Osvježavanje je dostupno uz naslov popisa članova.

U Supabase načinu rada popis zadržava naziv, opis i pristupni kod kreirane momčadi. Ogledni lokalni podaci ne koriste se za članove Supabase momčadi.

## Organizacija koda

- `domain/teams/TeamModels.kt` – `Team` s vlasnikom i brojem članova, `TeamMember` i zasebni `TeamRoster`.
- `domain/teams/TeamRepository.kt` – ugovor `getMyTeams()` / `getRoster(teamId)` i tipizirane pogreške.
- `data/teams/DefaultTeamRepository.kt` – provjera aktualne sesije, uloge i pripadnosti momčadi.
- `data/remote/SupabaseTeamListRepository.kt` – isti ugovor `TeamRepository` za Supabase, uz `DefaultTeamRepository` i Supabase izvor za momčadi i njihove članove.
- `data/teams/TeamDataSource.kt` – zamjenjivi izvor podataka koji dobiva provjerenu sesiju za buduće autentificirane HTTP zahtjeve.
- `data/teams/local/LocalTeamDataSource.kt` – dohvat iz lokalnog kataloga i inicijalno povezivanje oglednih momčadi s postojećim testnim trenerom.
- `data/teams/local/TeamStore.kt` i `EncryptedTeamStore.kt` – ugovor pohrane i JSON zapis šifriranih momčadi, članova i vlasnika.
- `data/teams/local/DemoTeamCatalog.kt` – početni sadržaj kataloga; koristi se i za Compose previews.
- `ui/teams/MyCreatedTeamsScreen.kt` – popis vlastitih momčadi i gumb za otvaranje članova po ID-u momčadi.
- `ui/auth/SquadSyncApp.kt` – navigacija popis → članovi → popis; odabrani ID čuva se kroz `rememberSaveable`.
- `ui/teams/TeamsViewModel.kt` – asinkrono učitavanje članova točno određenog ID-a, otkazivanje prethodnog zahtjeva i stanja učitavanja/pogreške. Nedostupna momčad ne zamjenjuje se prvom momčadi s popisa.
- `ui/teams/TeamsScreen.kt` – prikaz i callbackovi, bez izravnog pristupa pohrani ili mreži.
- `ui/teams/TeamsRoute.kt` – povezivanje ekrana i ViewModela. Pri povratku u vidljivo stanje ponovno se provjeravaju podaci; pri izlasku ili odlasku u pozadinu zahtjev se otkazuje i popis briše iz UI stanja.
- `di/AppContainer.kt` – zajedničke instance auth i team repozitorija, pohrane i lokalnog izvora.

Tok podataka je `TeamsScreen → TeamsViewModel → TeamRepository → TeamDataSource`, zatim lokalni `TeamStore` ili Supabase API.

## Provjera ovlasti

Identitet se uzima isključivo iz `AuthRepository`; pozivatelj repozitoriju ne šalje ID trenera. Za svaki dohvat potrebna je aktivna, neistekla sesija s tokenom i ulogom `COACH`. Repozitorij osvježava prijavu kroz postojeći auth sloj kako bi provjerio aktualne podatke računa i token.

`getMyTeams()` vraća samo momčadi čiji se `coachId` poklapa s ID-em prijavljenog korisnika. `getRoster(teamId)` prvo dohvaća metapodatke i provjerava vlasništvo, a tek zatim traži članove. Lokalni izvor dodatno provjerava vlasništvo pri samom čitanju članova.

Prije vraćanja rezultata ponovno se provjeravaju sesija, token, uloga i istek. Odjava, promjena korisnika ili istek tijekom zahtjeva ne mogu vratiti članove prethodnog korisnika. Pogreške ovlasti čiste prethodno prikazani popis.

## Supabase dohvat

Supabase izvor koristi postojeći autentificirani klijent. Tablica `momcadi` filtrira se po prijavljenom `trener_id`, a za članove i po odabranom ID-u momčadi. Članovi se čitaju ugniježđenim dohvatom `momcadi → clanovi_momcadi → korisnici`, s podacima `id`, `ime`, `prezime`, `email` i `uloga`. Nedostupan profil člana prijavljuje pogrešku umjesto uspješnog praznog popisa.

Za ovaj dohvat potrebne su strane veze između postojećih tablica i RLS pravila koja treneru dopuštaju čitanje vlastitih momčadi, njihovih članstava i pripadajućih profila. SQL shema i politike nisu mijenjane ovom izmjenom. Stvarni dohvat treba potvrditi s važećom trenerskom prijavom; pokušaj čitanja sheme postojećom lokalnom konfiguracijom 24. 11. 2026. vratio je HTTP 401.

## Provjera implementacije

Testovi provjeravaju otvaranje članova iz kartice momčadi, promjenu momčadi povratkom na popis, oba načina povratka, uklonjenu stavku u pregledu uloge, vlastitu i tuđu momčad, podatke sportaša, prazne prikaze, nedostatak/istek sesije, odjavu tijekom zahtjeva, trajnu pripadnost lokalnog kataloga te uklanjanje zastarjelog popisa. UI testovi koriste izolirane korisnike i podatke te ne prijavljuju se na Supabase.

Provjera 24. 11. 2026.: izgradnja i lint prolaze (bez lint pogrešaka, uz 18 upozorenja). Prolaze **83/83 jedinična testa** i **19/19 Android testova** na emulatoru `Pixel_8`, Android 16. Među Android testovima je deset UI provjera pregleda članova i pet integracijskih provjera šifrirane pohrane. Vizualno su provjerene snimke popisa momčadi s novim gumbima, članova odabrane momčadi i pregleda uloge bez stare stavke.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# Uz pokrenut emulator:
.\gradlew.bat :app:connectedDebugAndroidTest
```

Povijesni rezultati lokalne implementacije od 23. 11. 2026. i upute za Android Studio nalaze se u [izvještaju testiranja](TEAM_MEMBERS_TESTING.md). Stvarni Supabase dohvat i poslužiteljske ovlasti zahtijevaju zasebnu provjeru s trenerskom prijavom.
