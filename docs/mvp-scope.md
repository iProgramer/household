# MVP Scope

## Im MVP enthalten

### Haushalt & Personen
- Ein Haushalt mit genau 2 Personen
- Einfache Registrierung/Einladung (z.B. Einladungslink)
- Gleichberechtigte Nutzer, keine Rollen

### Aufgaben
- Aufgabe erstellen: Titel, optionales Datum, optionale Zuweisung
- Aufgabe ohne Zuweisung → jeder kann sie übernehmen/abhaken
- Aufgabe erledigen (abhaken)
- Wiederkehrende Aufgaben mit festen Intervallen:
  - Täglich, wöchentlich, alle 2 Wochen, monatlich, bestimmter Wochentag
- Erledigte wiederkehrende Aufgabe → nächste Instanz wird automatisch erzeugt

### Feste Termine
- Einfache Einträge mit Titel und Datum (z.B. "Gelber Sack")
- Wiederkehrend möglich (gleiche Intervalle wie Aufgaben)
- Passive Anzeige im Tages-/Wochenüberblick – kein Aufgabencharakter

### Projekte
- Projekt erstellen: Titel + Zielbeschreibung
- Flache Liste von Teilschritten (1 Ebene, keine Verschachtelung)
- Teilschritte sind wie Aufgaben: optional terminierbar und zuweisbar
- Fortschrittsanzeige (X von Y erledigt)
- Keine Beschränkung der Größe, aber Ziel hilft beim Abgrenzen

### Essensplan
- Pro Tag ein Freitext-Feld ("Was gibt's heute")
- Editierbar in der Wochenansicht (Sonntagsplanung)
- Sichtbar in der Tagesansicht

### Ansichten
- **Tagesansicht:** Heutige Aufgaben + Termine + Essensplan
- **Wochenansicht:** Aktuelle Woche, Tag für Tag. Offene/ungeplante Aufgaben sichtbar.
- **Projekte:** Liste aller Projekte mit Fortschritt, Drill-down in Teilschritte
- **Offene Aufgaben:** Alle unterminierten/unerledigten Aufgaben (Backlog)

---

## Bewusst nicht im MVP

| Thema | Grund |
|-------|-------|
| Automatische/rotierende Zuweisung | Overhead, Paar regelt das informell |
| Push-Benachrichtigungen | Technisch aufwendig, morgens Reinschauen reicht |
| Aufgaben-Historie / Statistiken | Kein Fairness-Tracking im MVP |
| Verschachtelte Teilaufgaben | 1 Ebene reicht, Komplexität vermeiden |
| Kalender-Import/Export | Termine werden manuell gepflegt |
| Kommentare/Notizen an Aufgaben | Titel muss reichen |
| Drag & Drop in Wochenansicht | Datum-Feld setzen reicht |
| Multi-Haushalt | Genau ein Paar |
| Gamification | Spätere Iteration |
| Budgets / Finanzen | Anderes Problemfeld |
| Einkaufslisten | Anderes Problemfeld |

---

## MVP-Validierungskriterien

Der MVP ist erfolgreich, wenn:
1. Beide Personen morgens in <10s sehen, was heute ansteht.
2. Die Sonntagsplanung in der App statt im Kopf/auf Zetteln passiert.
3. Wiederkehrende Aufgaben nicht mehr vergessen werden.
4. Mindestens ein Projekt in Teilschritten vorankommt, statt liegen zu bleiben.
