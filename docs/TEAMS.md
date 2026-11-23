# Momčadi i članovi – ekran i lokalni dohvat uz ovlasti

Implementirani su ekran s popisom članova i porukom za praznu momčad te **lokalni dohvat članova uz provjeru ovlasti**. Druga skica (`momcadi.png`) služi kao osnova, uz plavi sportski stil postojeće aplikacije. Komunikacija s backendom još nije uvedena.

## Otvaranje ekrana

Prijavi se kao trener (`trener@squadsync.hr` / `Trener123!`), zatim na profilu odaberi **Moje momčadi**. Strelica na zaglavlju ili sistemska tipka Natrag vraćaju na profil.

Testni trener dobiva dvije ogledne momčadi:

- **Seniori** – četiri sportaša s imenom, prezimenom, e-poštom i oznakom uloge.
- **Juniori** – prazna momčad koja prikazuje poruku da još nema članova.

Pri prvom dohvatu momčadi se povezuju sa stvarnim ID-em već spremljenog računa `trener@squadsync.hr`. Katalog se sprema u `teams.enc` preko postojeće AES-GCM pohrane u `noBackupFilesDir`. Postojeći katalog se ne prepisuje niti se vlasništvo mijenja prema korisniku koji trenutno traži podatke. Ogledni sportaši nisu povezani s registriranim računima.

Novi registrirani trener dobiva prazan popis jer mu još nije dodijeljena momčad. Prazna momčad i trener bez momčadi imaju odvojene poruke. Ekran također prikazuje učitavanje, pogrešku i ponovni pokušaj. Osvježavanje je dostupno uz naslov popisa članova.

## Organizacija koda

- `domain/teams/TeamModels.kt` – `Team` s vlasnikom i brojem članova, `TeamMember` i zasebni `TeamRoster`.
- `domain/teams/TeamRepository.kt` – ugovor `getMyTeams()` / `getRoster(teamId)` i tipizirane pogreške.
- `data/teams/DefaultTeamRepository.kt` – provjera aktualne sesije, uloge i pripadnosti momčadi.
- `data/teams/TeamDataSource.kt` – zamjenjivi izvor podataka koji dobiva provjerenu sesiju za buduće autentificirane HTTP zahtjeve.
- `data/teams/local/LocalTeamDataSource.kt` – dohvat iz lokalnog kataloga i inicijalno povezivanje oglednih momčadi s postojećim testnim trenerom.
- `data/teams/local/TeamStore.kt` i `EncryptedTeamStore.kt` – ugovor pohrane i JSON zapis šifriranih momčadi, članova i vlasnika.
- `data/teams/local/DemoTeamCatalog.kt` – početni sadržaj kataloga; koristi se i za Compose previews.
- `ui/teams/TeamsViewModel.kt` – asinkrono učitavanje kroz repozitorij, odabir momčadi, otkazivanje prethodnog zahtjeva i stanja učitavanja/pogreške. `SavedStateHandle` čuva samo odabrani ID.
- `ui/teams/TeamsScreen.kt` – prikaz i callbackovi, bez izravnog pristupa pohrani ili mreži.
- `ui/teams/TeamsRoute.kt` – povezivanje ekrana i ViewModela. Pri povratku u vidljivo stanje ponovno se provjeravaju podaci; pri izlasku ili odlasku u pozadinu zahtjev se otkazuje i popis briše iz UI stanja.
- `di/AppContainer.kt` – zajedničke instance auth i team repozitorija, pohrane i lokalnog izvora.

Tok podataka je `TeamsScreen → TeamsViewModel → TeamRepository → TeamDataSource → TeamStore`.

## Provjera ovlasti

Identitet se uzima isključivo iz `AuthRepository`; pozivatelj repozitoriju ne šalje ID trenera. Za svaki dohvat potrebna je aktivna, neistekla sesija s tokenom i ulogom `COACH`. Repozitorij osvježava prijavu kroz postojeći auth sloj kako bi provjerio aktualne podatke računa i token.

`getMyTeams()` vraća samo momčadi čiji se `coachId` poklapa s ID-em prijavljenog korisnika. `getRoster(teamId)` prvo dohvaća metapodatke i provjerava vlasništvo, a tek zatim traži članove. Lokalni izvor dodatno provjerava vlasništvo pri samom čitanju članova.

Prije vraćanja rezultata ponovno se provjeravaju sesija, token, uloga i istek. Odjava, promjena korisnika ili istek tijekom zahtjeva ne mogu vratiti članove prethodnog korisnika. Pogreške ovlasti čiste prethodno prikazani popis.

## Budući backend

U `AppContainer` zamijeni `LocalTeamDataSource` implementacijom `TeamDataSource` koja šalje `session.accessToken` backendu i mapira odgovor na iste domenske modele. Poslužitelj mora sam provjeravati identitet, ulogu i ovlasti nad momčadi; lokalna provjera nije zamjena za poslužiteljsku autorizaciju. Ekran i ViewModel ostaju povezani s istim sučeljem `TeamRepository`.

## Provjera implementacije

Pregled članova pokriven je s 31 jediničnim testom te 14 Android testova za UI i integraciju šifrirane pohrane. Provjeravaju se vlastita i tuđa momčad, podaci sportaša, odabir momčadi, prazni prikazi, nedostatak/istek sesije, odjava i promjena korisnika tijekom zahtjeva, trajna pripadnost kataloga te uklanjanje zastarjelog popisa.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# Uz pokrenut emulator:
.\gradlew.bat :app:connectedDebugAndroidTest
```

Pregled grana, pokrivenost acceptance kriterija, rezultati izvođenja i upute za Android Studio nalaze se u [izvještaju testiranja](TEAM_MEMBERS_TESTING.md). Testovi pokrivaju lokalnu implementaciju; Supabase dohvat momčadi i poslužiteljske ovlasti trebaju se provjeriti nakon njihove implementacije. Početna trenerska nadzorna ploča, kreiranje momčadi i dodavanje članova nisu dio ove izmjene.
