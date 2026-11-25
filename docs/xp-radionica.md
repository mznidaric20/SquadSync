# XP radionica

## XP-04 – Uvoz sportaša iz CSV datoteke

### TDD – RED faza

- Napisan je jedinični test `validCsvShouldReturnTwoAthletes` prije implementacije logike CSV parsera.
- Test provjerava učitavanje dva ispravna retka iz CSV datoteke sa zaglavljem `ime,prezime,email`.
- Očekivani rezultat: 2 ispravna retka i 0 neispravnih redaka.
- Dobiveni rezultat: 0 ispravnih redaka.
- Status: FAILED (očekivano).
- Razlog: parser još nema implementiranu logiku čitanja redaka.

### TDD – GREEN faza

- Predložena je minimalna implementacija CSV parsera koja preskače zaglavlje i prazne retke.
- Očekivani rezultat ponovljenog testa: 2 ispravna retka i 0 neispravnih redaka.
- Stvarni rezultat i status: potrebno potvrditi ponovnim pokretanjem testa.
- Validacija imena, prezimena i e-maila nije dio ovog TDD ciklusa.

### TDD ciklus 2 – Validacija imena

#### RED faza

- Test: csvWithoutFirstNameShouldBeInvalid
- Očekivani rezultat: 0 ispravnih redaka.
- Dobiveni rezultat: 1 ispravan redak.
- Status: FAILED (očekivano).
- Razlog: validacija imena još nije implementirana.
- Rezultat svih testova: 1 prolazi, 1 ne prolazi.

#### GREEN faza

- Implementirana validacija imena sportaša.
- Redci bez imena označavaju se kao neispravni.
- Rezultat: 2 testa prolaze, 0 neuspjelih.
- Status: PASSED.

### TDD ciklus 3 – Validacija prezimena

#### RED faza

- Test: csvWithoutLastNameShouldBeInvalid
- Očekivani rezultat: 0 ispravnih redaka.
- Dobiveni rezultat: 1 ispravan redak.
- Status: FAILED (očekivano).
- Razlog: validacija prezimena nije implementirana.
- Rezultat: 2 testa prolaze, 1 ne prolazi.

#### GREEN faza

- Implementirana validacija prezimena sportaša.
- Redci bez imena ili prezimena označavaju se kao neispravni.
- Rezultat: 3 testa prolaze, 0 neuspjelih.
- Status: PASSED.

### TDD ciklus 4 – Validacija nedostajućeg e-maila

#### RED faza

- Test: csvWithoutEmailShouldBeInvalid
- Očekivani rezultat: 0 ispravnih redaka.
- Dobiveni rezultat: 1 ispravan redak.
- Status: FAILED (očekivano).
- Razlog: validacija e-maila nije implementirana.
- Rezultat: 3 testa prolaze, 1 ne prolazi.


#### GREEN faza

- Implementirana provjera nedostajućeg e-maila.
- Redci bez e-maila označavaju se kao neispravni.
- Rezultat: 4 testa prolaze, 0 neuspjelih.
- Status: PASSED.

### TDD ciklus 5 – Validacija formata e-maila

#### RED faza

- Test: csvWithInvalidEmailShouldBeInvalid
- Očekivani rezultat: 0 ispravnih redaka.
- Dobiveni rezultat: 1 ispravan redak.
- Status: FAILED (očekivano).
- Razlog: provjera formata e-maila nije implementirana.
- Rezultat: 4 testa prolaze, 1 ne prolazi.

#### GREEN faza

- Implementirana validacija formata e-mail adrese.
- Za provjeru se koristi regularni izraz (Regex).
- Redci s neispravnim e-mailom označavaju se kao neispravni.
- Rezultat: 5 testova prolazi, 0 neuspjelih.
- Status: PASSED.

### TDD ciklus 6 – Validacija broja stupaca

#### RED faza

- Test: csvWithExtraColumnShouldBeInvalid
- Očekivani rezultat: 0 ispravnih redaka.
- Dobiveni rezultat: 1 ispravan redak.
- Status: FAILED (očekivano).
- Razlog: broj CSV stupaca nije provjeren.
- Rezultat: 5 testova prolazi, 1 ne prolazi.

#### GREEN faza

- Implementirana validacija broja CSV stupaca.
- Svaki redak mora sadržavati točno tri stupca.
- Redci s viškom ili manjkom stupaca označavaju
  se kao neispravni.
- Rezultat: 6 testova prolazi, 0 neuspjelih.
- Status: PASSED.

### TDD ciklus 7 – Validacija dupliciranih sportaša

#### RED faza

- Test: csvWithDuplicateEmailShouldBeInvalid
- Očekivani rezultat: 1 ispravan i 1 neispravan redak.
- Dobiveni rezultat: 2 ispravna retka.
- Status: FAILED (očekivano).
- Razlog: provjera duplikata nije implementirana.
- Rezultat: 6 testova prolazi, 1 ne prolazi.

#### GREEN faza

- Implementirana provjera dupliciranih e-mail adresa.
- Za pohranu jedinstvenih adresa koristi se MutableSet.
- Prvi ispravan zapis se prihvaća, a duplikati odbijaju.
- Rezultat: 7 testova prolazi, 0 neuspjelih.
- Status: PASSED.

### Test 8 – Validacija mješovitog CSV-a

- Test: mixedCsvShouldReturnCorrectValidationResults
- Ukupan broj redaka: 5.
- Očekivani rezultat: 2 ispravna i 3 neispravna retka.
- Dobiveni rezultat: 2 ispravna i 3 neispravna retka.
- Provjeren je i sadržaj ispravnih redaka.
- Status: PASSED.
- Svih 8 jediničnih testova uspješno prolazi.
- Dodatna implementacija nije bila potrebna.


### REFACTOR – CSV parser

- Validacija izdvojena u metodu isValidRow().
- Poboljšana čitljivost programskog koda.
- Postojeća funkcionalnost ostala je nepromijenjena.
- Rezultat: 8 uspješnih testova, 0 neuspjelih.



