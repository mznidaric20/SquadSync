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