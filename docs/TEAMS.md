# Momčadi i članovi – prvi subtask

Implementiran je subtask **Izrada ekrana s popisom članova, uključujući poruku za praznu momčad**. Druga skica (`momcadi.png`) služi kao osnova, uz plavi sportski stil postojeće aplikacije.

## Otvaranje ekrana

Prijavi se kao trener (`trener@squadsync.hr` / `Trener123!`), zatim na profilu odaberi **Moje momčadi**. Strelica na zaglavlju ili sistemska tipka Natrag vraćaju na profil.

Padajući izbornik sadrži dvije ogledne momčadi:

- **Seniori** – četiri sportaša s imenom, prezimenom, e-poštom i oznakom uloge.
- **Juniori** – prazna momčad koja prikazuje poruku da još nema članova.

Prikaz podržava i stanje bez ijedne momčadi. Popis članova skrola se i koristi stabilne identifikatore. Previews za popunjenu i praznu momčad, stanje bez momčadi, širi ekran i tamnu temu nalaze se uz `TeamsScreen`.

## Organizacija koda

- `domain/teams/TeamModels.kt` – modeli momčadi i članova, bez Android ovisnosti.
- `ui/teams/TeamsViewModel.kt` – nepromjenjivi `StateFlow` stanja ekrana i odabir momčadi. `SavedStateHandle` čuva identifikator odabrane momčadi za obnovu stanja.
- `ui/teams/TeamsScreen.kt` – prikaz i callbackovi, bez pristupa pohrani ili mreži.
- `ui/teams/TeamsRoute.kt` – povezivanje ViewModela s ekranom; ViewModel je odvojen ključem prijavljenog korisnika.
- `ui/teams/demo/DemoTeamRosters.kt` – ogledni podaci samo za prikaz prvog subtaska.

Ogledni sportaši nisu povezani s registriranim računima. Podaci se ne dohvaćaju s backenda i ne spremaju u listu računa. Kasnije se stvarni podaci mogu mapirati u iste modele i predavati stanju ekrana.

## Preostali subtaskovi

**Dohvat članova momčadi uz provjeru ovlasti** još nije implementiran. Prikaz gumba prema ulozi trenera dio je navigacije; ne predstavlja provjeru ovlasti nad konkretnom momčadi. Pristup stvarnim momčadima mora se provjeravati u budućem podatkovnom/backend sloju.

**Testiranje** ostaje zaseban subtask. U ovoj izmjeni provjeravaju se kompilacija i Android lint; nisu dodani testovi korisničke priče niti je implementirana početna trenerska nadzorna ploča, kreiranje momčadi ili dodavanje članova.
