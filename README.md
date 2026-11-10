# SquadSync

**SquadSync** je mobilna aplikacija za **trenere i sportaše** koja bi trebala olakšati organizaciju ekipe i praćenje treninga.

Ideja je da trener na jednom mjestu može planirati treninge, pratiti aktivnosti i napredak sportaša te dijeliti važne informacije s ekipom. Sportaši bi mogli vidjeti svoj raspored, treninge i druge informacije koje su im potrebne.

Projekt razvija tim **CodeNova** u sklopu kolegija **Analiza i razvoj programa (AIR)** na Fakultetu organizacije i informatike (FOI).

## Trenutačno stanje

Projekt je još u početnoj fazi razvoja. Na GitHubu se trenutačno nalazi osnovna Android aplikacija koja nakon pokretanja prikazuje **Hello Android!**. Opisane mogućnosti za trenere i sportaše zasad su planirane, a ne dovršene.

## Tehnologije

- **Android Studio** – razvoj aplikacije
- **Kotlin 2.2.10** – programski jezik
- **Jetpack Compose** – izrada korisničkog sučelja
- **Android Gradle Plugin 9.5.1** i **Gradle 9.6.0** – izgradnja projekta
- **Android SDK 37** – verzija za izgradnju aplikacije
- **JDK 25** – verzija zadana za Gradle
- **Git i GitHub** – zajednički rad na kodu

Aplikacija podržava Android uređaje od **API-ja 28** nadalje.

## Pokretanje aplikacije

1. Instaliraj [Android Studio](https://developer.android.com/studio) i [Git](https://git-scm.com/downloads).
2. U Android Studiju instaliraj **Android SDK 37** i **Android Emulator**.
3. Preuzmi projekt naredbama:

```bash
git clone https://github.com/mznidaric20/SquadSync.git
cd SquadSync
```

4. U Android Studiju odaberi **Open** i otvori mapu **SquadSync**.
5. Pričekaj da završi **Gradle Sync**.
6. Pokreni Android emulator s **API-jem 28 ili novijim**, odaberi konfiguraciju **app** i klikni **Run**.

Ako se prikaže **Hello Android!**, početna aplikacija je pokrenuta.

Detaljnije upute nalaze se u datoteci [docs/SETUP.md](docs/SETUP.md).

## Članovi tima

- **Filip Drvoderić**
- **Maksim Kos**
- **Roberto Šandro**
- **Matija Žnidarić**

## Autor uputa

**Filip Drvoderić**
