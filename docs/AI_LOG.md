# AI LOG

## XP-04 – Uvoz sportaša iz CSV datoteke

AI alat: ChatGPT

### RED faza

- **Upiti:** „Što znači napisati test za ispravan CSV?” i „Daj mi točne korake što kamo moram staviti.”
- **Prijedlog AI-ja:** napisati JUnit test za dva ispravna CSV retka i kreirati minimalnu strukturu parsera bez logike obrade.
- **Odluka i postupak:** kreiran je test `CsvParserTest` i pokrenut u Android Studiju prije implementacije logike parsera.
- **Rezultat:** test očekivano nije prošao (expected: 2, actual: 0).

### GREEN faza

- **Upit:** „Ok idemo dalje.”
- **Prijedlog AI-ja:** implementirati minimalno čitanje CSV-a preskakanjem zaglavlja i praznih redaka, bez dodatne validacije.
- **Provjera:** potrebno ponovno pokrenuti postojeći jedinični test i potvrditi prolaznost.
- **Rezultat:** još nije potvrđen.

### XP-04 – Validacija imena – RED faza

- Upit AI-ju: Koji je sljedeći test?
- Prijedlog AI-ja: Testiranje CSV retka bez imena.
- Odluka tima: Napisati test prije implementacije validacije.
- Rezultat: Test očekivano ne prolazi.

### XP-04 – Validacija imena – GREEN faza

- Upit AI-ju: Nastavak GREEN faze validacije imena.
- Prijedlog AI-ja: Provjera prvog CSV stupca
  metodom isBlank().
- Odluka tima: Implementirati minimalnu validaciju imena.
- Rezultat: 

### XP-04 – Validacija prezimena – RED faza

- Upit AI-ju: Idemo na idući test.
- Prijedlog AI-ja: Testiranje CSV retka bez prezimena.
- Odluka tima: Napisati test prije implementacije.
- Rezultat: Test očekivano ne prolazi.

### XP-04 – Validacija prezimena – GREEN faza

- Upit AI-ju: Nastavak nakon RED testa prezimena.
- Prijedlog AI-ja: Provjera drugog CSV stupca
  metodama getOrElse() i isBlank().
- Odluka tima: Proširiti postojeću validaciju.
- Rezultat: Sva tri testa uspješno prolaze.

### XP-04 – Validacija nedostajućeg e-maila – RED

- Upit AI-ju: Nastavak na sljedeći test.
- Prijedlog AI-ja: Testiranje CSV retka bez e-maila.
- Odluka tima: Napisati test prije implementacije.
- Rezultat: Test očekivano ne prolazi.


### XP-04 – Validacija nedostajućeg e-maila – GREEN

- Upit AI-ju: Nastavak nakon RED testa e-maila.
- Prijedlog AI-ja: Provjera trećeg CSV stupca
  metodama getOrElse() i isBlank().
- Odluka tima: Proširiti postojeću validaciju.
- Rezultat: Sva četiri testa uspješno prolaze.


### XP-04 – Validacija formata e-maila – RED

- Upit AI-ju: Nastavak nakon četvrtog TDD ciklusa.
- Prijedlog AI-ja: Testiranje neispravnog formata e-maila.
- Odluka tima: Napisati test prije implementacije validacije.
- Rezultat: Test očekivano ne prolazi.

### XP-04 – Validacija formata e-maila – GREEN

- Upit AI-ju: Dalje.
- Prijedlog AI-ja: Korištenje regularnog izraza
  za provjeru formata e-mail adrese.
- Odluka tima: Proširiti postojeću validaciju.
- Rezultat: Svih pet testova uspješno prolazi.

### XP-04 – Validacija broja stupaca – RED

- Upit AI-ju: Dalje.
- Prijedlog AI-ja: Testiranje CSV retka
  s viškom stupaca.
- Odluka tima: Napisati test prije implementacije.
- Rezultat: Test očekivano ne prolazi.

### XP-04 – Validacija broja stupaca – GREEN

- Upit AI-ju: Dalje.
- Prijedlog AI-ja: Provjera broja stupaca
  pomoću columns.size != 3.
- Odluka tima: Odbaciti retke s neispravnim
  brojem stupaca prije daljnje validacije.
- Rezultat: Svih šest testova uspješno prolazi.



