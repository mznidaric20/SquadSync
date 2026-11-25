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
