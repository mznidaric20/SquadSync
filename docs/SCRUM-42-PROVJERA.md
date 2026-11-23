# SCRUM-42 — provjera lokalnog paketa

Ovaj paket je pripremljen za granu `dev` repozitorija `mznidaric20/SquadSync` na commit-u `8ee6285349a02c2fd3e4f9be3c720d3f4d89198e` (provjereno 23. 11. 2026.).

## Što je implementirano

- Model ovlasti `RoleAccessPolicy` koristi **postojeći** `UserRole` (trener / sportaš).
- Nakon prijave prikazuju se dvije donje kartice: **Profil** (postojeći ekran) i **Moja uloga** (uloge i njihove mogućnosti).
- Na drugom ekranu prikazuju se samo kartice dopuštene trenutačnoj ulozi; nisu lažni gumbi za funkcionalnosti koje još nisu implementirane.
- Nepoznata uloga učitana iz Supabasea sada se odbija, umjesto da se automatski protumači kao sportaš.
- Dodani su unit testovi za matricu uloga te za obradu nepoznate uloge.

## Što ovaj paket NE tvrdi da je dovršeno

- Nisu kreirani novi backend API endpointi. Trenutačni `dev` sadrži samo prijavu i registraciju te profil; funkcionalnosti momčadi razvijaju drugi članovi tima.
- RLS pravila iz Confluence dokumentacije **nisu provjerena na stvarnoj Supabase instanci**. Lokalna provjera uloge ne može zamijeniti poslužiteljsku autorizaciju.
- Zbog toga se subtask **SCRUM-44** i puni kriteriji prihvaćanja **SCRUM-42** ne smiju označiti kao `Done` bez dodatne provjere Supabasea i integracije funkcionalnosti momčadi.
- Gradle izgradnja i Android instrumentacijski testovi nisu izvršeni tijekom izrade paketa (radno okruženje nema preuzete ovisnosti i Android SDK).

## Lokalna provjera

1. U Android Studiju otvori projekt na grani `feature/SCRUM-42-role-access`.
2. Pokreni Gradle Sync.
3. Pokreni aplikaciju u emulatoru.
4. S lokalnom autentifikacijom prijavi se kao trener `trener@squadsync.hr` / `Trener123!` i provjeri da kartica **Moja uloga** prikazuje samo mogućnosti trenera.
5. Odjavi se, prijavi kao sportaš `sportas@squadsync.hr` / `Sportas123!` i provjeri da vidi samo mogućnosti sportaša.
6. Provjeri da je profil ostao isti, da radi odjava i da se prijava pamti pri ponovnom pokretanju.
7. Pokreni testove:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

## Poslužiteljska autorizacija — obavezna zasebna provjera

Confluence stranice: `Tablica Korisnici`, `Tablica Momčadi` i `Tablica Članovi momčadi` opisuju Supabase RLS pravila. Njihove deklaracije nisu dokaz da su stvarno aktivne na udaljenoj bazi.

Provjera definiranih politika (samo čitanje; Supabase SQL Editor):

```sql
select schemaname, tablename, policyname, cmd, roles, qual, with_check
from pg_policies
where schemaname = 'public'
  and tablename in ('korisnici', 'momcadi', 'clanovi_momcadi')
order by tablename, policyname;
```

Ta SQL provjera prikazuje konfiguraciju, ali **ne dokazuje** da zahtjevi s tokenom stvarnog korisnika bivaju ispravno odbijeni. Potrebni su integracijski testovi s prijavljenim trenerom A, trenerom B i sportašem:

- trener A ne može čitati ili mijenjati momčad trenera B;
- sportaš ne može kreirati momčad niti čitati tuđe redove u `momcadi`;
- trener vidi samo članove momčadi koje vodi;
- sportaš vidi samo vlastita članstva, ali može se pridružiti momčadi kroz odgovarajuću RPC funkciju;
- nijedan prijavljeni korisnik ne može promijeniti svoj stupac `uloga` putem javnog API-ja.

RLS i RPC provjere trebaju se izvesti s korisničkim `anon`/publishable ključem i stvarnim sesijskim JWT-om, ne s administratorskim `service_role` ključem, jer administratorski pristup može zaobići RLS.

## Integracija s drugim zadacima

Kada kolege spoje ekrane i repozitorije za momčadi u `dev`, karticu **Moja uloga** treba povezati sa stvarnim ekranima. Svaki novi poziv zaštićenog API-ja mora se provjeravati RLS pravilima na Supabaseu, ne samo s `RoleAccessPolicy.canAccess()` u Androidu.
