# Iteration Plan

Kleine, vertikal geschnittene Iterationen. Jede liefert nutzbaren Mehrwert.

---

## Iteration 1: Aufgaben-Grundgerüst

**Ziel:** Aufgaben erstellen, sehen und abhaken – für eine Person.

- Haushalt + Person anlegen (Seed/Setup, kein Onboarding-Flow)
- Aufgabe erstellen (Titel + optionales Datum)
- Tagesansicht: heutige Aufgaben anzeigen
- Aufgabe abhaken
- Einfaches, funktionales UI (kein Styling-Fokus)

**Ergebnis:** Eine Person kann ihre Tagesaufgaben verwalten.

---

## Iteration 2: Zweite Person + Zuweisung

**Ziel:** Haushalt wird geteilt.

- Zweite Person einladen (einfacher Mechanismus, z.B. Token/Link)
- Aufgaben optional einer Person zuweisen
- Tagesansicht zeigt Zuweisung an
- Authentifizierung (Login für beide)

**Ergebnis:** Beide sehen die gleichen Aufgaben, wissen wer dran ist.

---

## Iteration 3: Wochenansicht

**Ziel:** Die Woche überblicken und planen.

- Wochenansicht: 7 Tage mit Aufgaben
- Bereich "Ungeplant" mit offenen Aufgaben ohne Datum
- Aufgabe Datum zuweisen (aus Wochenansicht heraus)

**Ergebnis:** Sonntagsplanung wird möglich (ohne Essen/Projekte).

---

## Iteration 4: Wiederkehrende Aufgaben

**Ziel:** Routinen abbilden.

- Wiederholungsregel bei Aufgabenerstellung setzen
- Intervalle: täglich, wöchentlich, 14-tägig, monatlich, bestimmter Wochentag
- Beim Abhaken: nächste Instanz automatisch erzeugen
- Wiederkehrende Aufgaben in Tages-/Wochenansicht korrekt anzeigen

**Ergebnis:** "Bad putzen jeden Samstag" funktioniert automatisch.

---

## Iteration 5: Feste Termine

**Ziel:** Termine sichtbar machen.

- Feste Termine anlegen (Titel + Datum)
- Optional wiederkehrend (gleiche Intervalle)
- Anzeige in Tages- und Wochenansicht (visuell von Aufgaben unterscheidbar)
- Nicht abhakbar

**Ergebnis:** "Morgen Gelber Sack" ist sichtbar ohne eigene Aufgabe.

---

## Iteration 6: Projekte

**Ziel:** Größere Vorhaben strukturieren.

- Projekt erstellen (Titel + Ziel)
- Teilschritte anlegen (= Aufgaben mit Projektzugehörigkeit)
- Teilschritte terminieren und zuweisen
- Projektliste mit Fortschrittsanzeige
- Projekt abschließen

**Ergebnis:** "Keller aufräumen" hat Struktur und kommt voran.

---

## Iteration 7: Essensplan

**Ziel:** Wochenplanung komplett machen.

- Freitext-Feld pro Tag
- Editierbar in der Wochenansicht
- Anzeige in der Tagesansicht

**Ergebnis:** "Was kochen wir heute?" ist beantwortet.

---

## Iteration 8: Backlog-Ansicht + Polish

**Ziel:** Überblick über alles und UX-Feinschliff.

- "Alles"-Ansicht: alle offenen Aufgaben, gruppiert nach Zeitraum
- Filter: meine / alle / unzugewiesen
- UI-Polish: konsistentes Design, mobile Optimierung
- Quick-Action "+" global verfügbar

**Ergebnis:** App ist vollständig nutzbar für den Alltag.

---

## Danach (Post-MVP)

- Push-Benachrichtigungen
- Aufgaben-Historie / Statistiken
- Drag & Drop in Wochenansicht
- Kalender-Import (iCal für Mülltermine)
- Gamification-Elemente
- Dark Mode
