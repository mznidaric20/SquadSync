# SCRUM-42 — Implementacija i provjera kontrole pristupa

**Projekt:** SquadSync  
**Tim:** CodeNova  
**Grana:** `feature/SCRUM-42-role-access`  
**Ciljna grana:** `dev`  
**Datum posljednje provjere:** 24. 11. 2026.

## 1. Implementirane funkcionalnosti

- Implementiran je `RoleAccessPolicy` za upravljanje pravima pristupa prema korisničkim ulogama (`Trener` i `Sportaš`).
- Nakon prijave dostupne su kartice **Profil** i **Moja uloga**.
- Trener vidi mogućnosti kreiranja momčadi, pregleda vlastitih momčadi i njihovih članova.
- Sportaš vidi mogućnosti pridruživanja momčadi i pregleda vlastitih članstava.
- Prikaz mogućnosti prilagođen je ulozi prijavljenog korisnika.
- Nepoznata uloga iz Supabasea odbija se umjesto automatskog dodjeljivanja uloge sportaša.
- Dodani su jedinični testovi provjere korisničkih prava i mapiranja uloga.

Prikazane mogućnosti zasad nisu sve povezane sa stvarnim funkcionalnostima momčadi koje implementiraju ostali članovi tima.

## 2. Lokalna provjera Android aplikacije

U Android Studiju izvršena je naredba:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

**Rezultat:** `BUILD SUCCESSFUL`

Potvrđeno je:
- Uspješna Gradle izgradnja aplikacije.
- Uspješno izvršavanje jediničnih testova.
- Uspješno povezivanje aplikacije sa Supabase autentifikacijom tijekom ručnih provjera prijave.
- Ispravno prepoznavanje korisničkih uloga trenera i sportaša.

Android instrumentacijski testovi nisu zasebno potvrđeni.

## 3. Provjera Supabase RLS pravila

Dana 24. 11. 2026. provedene su provjere aktivnih RLS pravila nad tablicama:

- `public.korisnici`
- `public.momcadi`
- `public.clanovi_momcadi`

Potvrđeno je da je RLS omogućen na sve tri tablice i da postoje odgovarajuće politike kontrole pristupa.

Tijekom testiranja otkrivena je rekurzija u RLS pravilu za kreiranje momčadi. Pravilo je ispravljeno korištenjem funkcije `private.je_trener()`, nakon čega su ponovljene provjere prošle.

### Rezultati SQL testiranja

| Test | Rezultat |
|---|---|
| Sportaš ne može kreirati momčad | Uspješno |
| Trener može kreirati vlastitu momčad | Uspješno |
| Trener ne može kreirati momčad drugog trenera | Uspješno |
| Trener vidi vlastitu momčad | Uspješno |
| Trener ne vidi tuđu momčad | Uspješno |
| Trener ne vidi članstva tuđe momčadi | Uspješno |
| Sportaš ne vidi tuđa članstva | Uspješno |
| Sportaš vidi vlastito članstvo | Uspješno |
| Trener može vidjeti profil člana svoje momčadi | Uspješno |
| Trener ne može izvršiti RPC pridruživanja sportaša | Uspješno |
| Sportaš ne može izravno dodavati članstva | Uspješno |

SQL testovi koristili su simulaciju autentificiranih korisnika unutar transakcija. Za operacije koje su mogle promijeniti podatke korišten je `ROLLBACK`.

## 4. Provjera stvarnog Supabase API-ja

Provedene su dodatne provjere pomoću PowerShella, uz stvarnu prijavu testnih korisnika i njihove pristupne tokene.

### Dohvat podataka

| Korisnik | Tablica | Rezultat |
|---|---|---|
| Sportaš | `momcadi` | 0 vidljivih momčadi |
| Trener | `momcadi` | 1 vidljiva momčad |
| Sportaš | `korisnici` | 1 vidljiv profil |
| Trener | `korisnici` | 1 vidljiv profil |
| Sportaš | `clanovi_momcadi` | 0 vidljivih članstava |
| Trener | `clanovi_momcadi` | 0 vidljivih članstava |

Rezultati odgovaraju očekivanim pravima pristupa za testne račune i postojeće podatke u bazi.

### Provjera zabranjenih operacija

| Operacija | Rezultat |
|---|---|
| Trener pokušava pozvati `pridruzi_se_momcadi` | `NIJE_SPORTAS` |
| Sportaš koristi neispravan pristupni kod | `NEISPRAVAN_KOD` |
| Sportaš pokušava promijeniti svoju ulogu | HTTP 403, kod 42501 |
| Sportaš pokušava izravno dodati članstvo | Odbijeno, kod 42501 |

**Svi navedeni API testovi imali su očekivane rezultate.**

Pozitivni API test dohvata vlastitog članstva nije proveden jer nije bila dostupna prijava na račun postojećeg člana. Isti scenarij uspješno je provjeren SQL simulacijom.

## 5. Preostali zadaci i ograničenja

- Povezati prikazane mogućnosti trenera i sportaša sa stvarnim ekranima momčadi.
- Uskladiti navigaciju i zajedničke datoteke s implementacijama ostalih članova tima.
- Nakon spajanja grana ponovno izvršiti izgradnju i testiranje aplikacije.
- Provjeriti cjelokupan korisnički tijek za kreiranje momčadi, pridruživanje i pregled članova.
- Dokumentirati i verzionirati promjene Supabase RLS pravila koje su ručno provedene u SQL Editoru.

Lokalna kontrola pristupa nije zamjena za poslužiteljsku autorizaciju. Zaštita podataka mora ostati provedena kroz Supabase RLS pravila.

## 6. Zaključak

Implementirana je osnovna kontrola pristupa prema korisničkim ulogama, uz uspješnu Gradle izgradnju i jedinične testove.

Provedene SQL i API provjere potvrđuju očekivana ograničenja pristupa za testirane scenarije.

**Implementacija je spremna za integraciju u granu `dev`, uz napomenu da povezivanje s ostalim funkcionalnostima momčadi i završno integracijsko testiranje još nisu dovršeni.**