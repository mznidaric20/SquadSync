# SquadSync – postavljanje razvojne okoline

**Autor:** Filip Drvoderić

Ovdje su koraci za preuzimanje projekta SquadSync i pokretanje Android aplikacije u emulatoru.

## 1. Potrebni programi

Instaliraj sljedeće programe:

- **Android Studio:** [službena stranica za preuzimanje](https://developer.android.com/studio)
- **Git:** [službena stranica za preuzimanje](https://git-scm.com/downloads)

Kroz Android Studio potrebno je instalirati i **Android SDK 37** te **Android Emulator**.

Projekt koristi ove verzije:

- **Gradle:** 9.6.0
- **Android Gradle Plugin:** 9.5.1
- **Kotlin:** 2.2.10
- **Jetpack Compose BOM:** 2026.02.01
- **Gradle Daemon JDK:** 25
- **Android SDK za izgradnju:** 37
- **Minimalni Android API:** 28

**Gradle ne moraš posebno instalirati** jer projekt sadrži Gradle Wrapper. Za prvo preuzimanje potrebnih alata i ovisnosti treba ti internet.

## 2. Preuzimanje projekta

Otvori terminal u mapi u koju želiš spremiti projekt i pokreni:

```bash
git clone https://github.com/mznidaric20/SquadSync.git
cd SquadSync
```

## 3. Otvaranje u Android Studiju

1. Pokreni **Android Studio**.
2. Klikni **Open** i odaberi mapu **SquadSync**.
3. Pričekaj da završi **Gradle Sync**.
4. Ako Android Studio traži dodatne SDK komponente, instaliraj ih.

Ako nedostaje **Android SDK 37**, otvori **Tools > SDK Manager**, pronađi **Android API 37** i instaliraj ga.

## 4. Provjera JDK-a

U datoteci **gradle/gradle-daemon-jvm.properties** projekt ima zadanu verziju **JDK 25** za Gradle Daemon.

Za provjeru u Android Studiju otvori **File > Settings > Build, Execution, Deployment > Build Tools > Gradle** (na macOS-u **Android Studio > Settings**). Ovisno o verziji Android Studija, ovdje možeš vidjeti postavku **Gradle JDK** ili postavke povezane s **Gradle Daemon JVM**.

Verziju možeš dodatno provjeriti iz terminala u glavnoj mapi projekta:

**Windows:**

```powershell
.\gradlew.bat --version
```

**Linux i macOS:**

```bash
./gradlew --version
```

Provjeri piše li **Gradle 9.6.0** i je li za **Daemon JVM** zadana verzija **25**. Ako projekt ne može pronaći odgovarajući JDK, provjeri poruku pogreške; Gradle ga može preuzeti automatski ako je to omogućeno i postoji internetska veza.

## 5. Postavljanje emulatora

1. Otvori **Tools > Device Manager**.
2. Klikni **Create Device** i odaberi mobitel, primjerice neki Pixel.
3. Odaberi Android sistemsku sliku s **API-jem 28 ili novijim** (primjerice API 35 ili 36).
4. Ako slika nije instalirana, preuzmi je.
5. Dovrši postavljanje i pokreni emulator.

**Napomena:** Za izgradnju projekta potreban je **SDK 37**, ali emulator ne mora koristiti API 37. Dovoljna je kompatibilna verzija od **API-ja 28** nadalje.

## 6. Pokretanje aplikacije

1. U Android Studiju odaberi konfiguraciju **app**.
2. Odaberi pokrenuti emulator.
3. Klikni **Run**.
4. Pričekaj da se aplikacija instalira i otvori.

Ako na zaslonu piše **Hello Android!**, aplikacija je pokrenuta.

## 7. Provjera izgradnje

Projekt možeš izgraditi i iz terminala u glavnoj mapi.

**Windows:**

```powershell
.\gradlew.bat :app:assembleDebug
```

**Linux i macOS:**

```bash
./gradlew :app:assembleDebug
```

Ako se pojavi **BUILD SUCCESSFUL**, izgradnja je uspjela.

## 8. Lokalne postavke

Android Studio obično sam napravi datoteku **local.properties** s putanjom do Android SDK-a. Ako Android Studio ne pronalazi SDK, provjeri je li ispravno instaliran i postavljen.

**Datoteku **local.properties**, lozinke i API ključeve nemoj slati na GitHub.**

## 9. Najčešći problemi

- **Android SDK nije pronađen:** provjeri **SDK Manager** i datoteku **local.properties**.
- **Nedostaje Android SDK 37:** instaliraj ga kroz **SDK Manager**.
- **Gradle Sync ne uspijeva:** provjeri internetsku vezu, JDK i poruku pogreške.
- **Pogreška vezana uz Javu:** provjeri postavke Gradlea i zahtjev za **JDK 25**.
- **Emulator se ne pokreće:** provjeri instaliranu sistemsku sliku i je li omogućena virtualizacija.

## 10. Provjera da sve radi

Postavljanje je završeno kada **Gradle Sync** uspješno završi, emulator se pokrene i aplikacija prikaže **Hello Android!**.
