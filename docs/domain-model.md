# Domain Model

Fachliches Modell – keine technische Implementierung.

## Kernbegriffe

### Haushalt (Household)
- Besteht aus genau 2 Personen
- Alle Objekte gehören zum Haushalt (geteilter Kontext)
- Es gibt kein "privat" – alles ist für beide sichtbar

### Person (Member)
- Gehört zu genau einem Haushalt
- Kann Aufgaben erstellen, sich zuweisen, erledigen
- Gleichberechtigt, keine Unterscheidung in Rollen

### Aufgabe (Task)
- Titel (Pflicht)
- Datum (optional) – wann soll sie erledigt werden?
- Zugewiesen an (optional) – wer macht es?
- Status: offen | erledigt
- Kann eigenständig sein oder zu einem Projekt gehören
- Kann wiederkehrend sein (→ Wiederholungsregel)

### Wiederholungsregel (Recurrence)
- Gehört zu einer Aufgabe
- Definiert das Intervall: täglich | wöchentlich | 14-tägig | monatlich | bestimmter Wochentag
- Beim Erledigen einer wiederkehrenden Aufgabe wird die nächste Instanz erzeugt
- Einfache Vorwärts-Erzeugung, keine Historie vergangener Instanzen
- Beispiele: Saugen (wöchentlich), Gelbe Säcke (14-tägig), Fenster putzen (monatlich)

### Projekt (Project)
- Titel (Pflicht)
- Ziel (kurze Beschreibung: Was ist "fertig"?)
- Liste von Teilschritten (= Aufgaben, die zum Projekt gehören)
- Fortschritt ergibt sich aus erledigten vs. offenen Teilschritten
- Status: aktiv | abgeschlossen

### Essensplan-Eintrag (Meal Note)
- Ein Freitext pro Tag
- Gehört zum Haushalt, nicht zu einer Person
- Kein eigenes Datenmodell nötig – einfach Tagesnotiz

---

## Beziehungen

```
Haushalt
├── Person (genau 2)
├── Aufgabe (0..n)
│   ├── optional: Wiederholungsregel
│   └── optional: gehört zu Projekt
├── Projekt (0..n)
│   ├── Ziel
│   └── Teilschritte (= Aufgaben)
└── Essensplan-Eintrag (0..1 pro Tag)
```

## Invarianten

1. Eine Aufgabe gehört zu maximal einem Projekt.
2. Teilschritte eines Projekts sind normale Aufgaben mit einer Projektzugehörigkeit.
3. Eine wiederkehrende Aufgabe existiert immer nur als aktuelle Instanz (kein Backlog vergangener Instanzen).
4. Zuweisung ist immer optional und jederzeit änderbar.
5. Der Essensplan-Eintrag ist pro Tag eindeutig (max. 1 Eintrag).
