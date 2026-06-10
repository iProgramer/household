# Feature-Ideen (Backlog)

Kurze Notizen zu Ideen, die noch nicht umgesetzt sind. Kein Anspruch auf Vollständigkeit —
primär als Gedächtnisstütze für spätere Planung.

---

## Bug: Task-Cards haben keine einheitliche Höhe (Mobile)

**Problem:** Die Breite/Höhe der Task-Karten variiert je nach Inhalt — kurze Titel ergeben
schmale Karten, lange Titel oder viele Badges (Projekt, Member, Wiederholung) breite.
Das wirkt auf Mobile unruhig und inkonsistent.

**Gewünschtes Verhalten:** Alle Karten haben eine feste, einheitliche Höhe.
Texte werden bei Bedarf abgekürzt (`…`), Elemente ggf. zweizeilig oder kleiner.
Unabhängig vom Inhalt soll die Karte immer gleich groß aussehen.

**Mögliche Umsetzung:**
- Feste Höhe auf `.task-item` (z.B. `min-height` oder festes `height`)
- Titel: bereits `text-overflow: ellipsis` vorhanden, ggf. `max-width` anpassen
- Projekt-Badge: `max-width` begrenzen, Overflow mit `…`
- Member-Badge + Menü-Button: `flex-shrink: 0` sicherstellen
- Zweizeiligkeit gezielt erlauben (z.B. Titel wrappen, Rest einzeilig)

**Reifegrad:** Konkret und umsetzbar, Designentscheidung (feste Höhe vs. zweizeilig)
muss noch getroffen werden.

---

## Drag & Drop — Aufgaben zwischen Wochentagen verschieben

**Kontext:** Wochenansicht (Übersicht-Modus), 7 Tages-Karten nebeneinander.

**Idee:** Eine Aufgabe per Drag & Drop von einem Tag auf einen anderen ziehen.
Im Hintergrund wird `PATCH /api/tasks/{id}` mit dem neuen Datum aufgerufen —
das Backend unterstützt das bereits vollständig.

**Varianten:**

| Variante | Beschreibung | Aufwand |
|----------|-------------|---------|
| A – HTML5 nativ | `draggable` + Browser-API, kein Paket | ~2h, aber kein Touch-Support |
| B – `svelte-dnd-action` | Library für Svelte, Touch + Maus, aktiv gepflegt | ~3h, empfohlen |

**Verworfene Variante:** 6-Punkte-Handle à la Notion (Sortierreihenfolge innerhalb eines Tages).
Erfordert eine `position`-Spalte in der DB und mehr Scope — lohnt sich erst wenn
Reihenfolge innerhalb eines Tages wirklich gebraucht wird.

**Empfehlung:** Variante B (`svelte-dnd-action`) sobald der Bedarf da ist.
Kein Backend-Aufwand nötig.

---


## BDD-Tests mit Cucumber (technische Verbesserung)

**Kontext:** Aktuell werden Szenario-Tests als plain Kotlin/JUnit geschrieben (`MealScenarioTest` etc.).
BDD würde die Tests in natürlicher Sprache (Gherkin) ausdrücken — lesbar auch ohne Kotlin-Kenntnisse.

**Framework-Optionen:**
- **Cucumber (JVM)** — Klassiker, Gherkin-Syntax, große Community
- **Kotest BDD** — Kotlin-nativ, `given/when/then` als DSL, kein separates Gherkin-File

**Wann sinnvoll:**
Sobald Nicht-Entwickler Akzeptanzkriterien lesen oder mitformulieren sollen.
Für ein reines Entwickler-Projekt ist der Overhead (Gherkin-Dateien + Step-Definitions) aktuell höher als der Nutzen.

**Aufwand:** ~1 Tag für Migration der bestehenden Szenario-Tests auf Cucumber.

---

## Statistiken & Gamification — Motivation durch Daten und Erfolge

**Kontext:** Inspiration: Garmin Connect — im Nachgang analysieren, Fortschritt sichtbar machen,
Motivation aufrechterhalten. Für eine Haushalts-App deutlich simpler, aber das Prinzip ist dasselbe.

**Zwei mögliche Richtungen (können getrennt umgesetzt werden):**

### A – Statistik-Dashboard

Kennzahlen über erledigte Aufgaben, Auslastung, Verteilung zwischen den Personen:

- Erledigte Aufgaben pro Tag / Woche / Monat
- Verhältnis Person A vs. Person B (Fairness-Übersicht)
- Erledigungsquote: wie viele geplante Tasks wurden tatsächlich abgehakt
- **Eingeplant vs. aufgeschoben:** wie viele Aufgaben wurden am geplanten Tag erledigt vs. verschoben oder liegen geblieben
- Aufgaben nach Kategorie / Projekt
- Aktivster Wochentag

Technisch: eigene Statistik-Endpunkte (Aggregation über Task-Historie) oder clientseitig
aus vorhandenen Daten berechnen. Erfordert ggf. Soft-Delete statt Hard-Delete,
damit erledigte Aufgaben in der Historie bleiben.

### B – Gamification & kontextuelle Benachrichtigungen

Kleine Erfolge sichtbar machen und beide Personen motivieren:

- **Rekorde:** "Neuer Rekord — 12 Aufgaben an einem Tag erledigt!"
- **Streaks:** "Ihr seid seit 5 Tagen am Stück produktiv"
- **Sofort-Feedback:** "Person Z hat gerade alle ihre Aufgaben abgehakt 🎉"
- **Morgen-Bericht** (z.B. 8 Uhr): "Guten Morgen — heute stehen 4 Aufgaben an"
- **Abend-Nudge** (z.B. 20 Uhr): "Noch 2 Aufgaben offen — fast geschafft!"
- **Backlog-Challenge:** "Du hast noch 27 Aufgaben im Backlog — schaffst du bis Mittwoch 10 davon?"

Technisch: Push-Benachrichtigungen erfordern Service Worker (PWA) + Web Push API
oder eine native App. Kontextuelle Meldungen innerhalb der App (ohne Push) sind
deutlich einfacher umsetzbar.

**Reifegrad:** Idee noch nicht ausgereift — zuerst klären, ob A oder B priorisiert wird,
bevor ein konkreter Implementierungsplan entsteht.

---

## Neustrukturierung der App nach Nutzungsmodus

**Kontext:** Die aktuelle Navigation orientiert sich an Datenbereichen (Heute, Woche, Projekte,
Mahlzeiten…). Eine alternative Strukturidee: die App orientiert sich daran, *wie* man sie nutzt —
also am mentalen Modus des Nutzers, nicht an der Datenstruktur.

**Grobe Nutzungsmodi (noch nicht fertig gedacht):**

- **Planen** (z.B. Sonntagabend): Woche überblicken, Aufgaben einteilen, Mahlzeiten planen,
  Projekte voranbringen. Alles was mit "was kommt auf uns zu?" zu tun hat.
- **Abarbeiten** (unter der Woche, täglich): Fokus auf heute — was ist dran, was hake ich ab?
  Minimale Ablenkung, klarer Fokus.
- **Erfassen** (jederzeit, spontan): Schnell etwas notieren, das nicht verloren gehen soll —
  neue Aufgabe, Mahlzeiten-Idee, Projektidee. Ohne großen Kontext.

**Mögliche Konsequenzen für die Navigation:**
Statt der heutigen Tabs (Heute / Woche / Projekte / Alles / Essen) könnte es
Modi-orientierte Einstiegspunkte geben — z.B. "Planen", "Heute", "Erfassen".
Die Daten dahinter bleiben dieselben, aber der Zugang ändert sich.

**Verwandtes Konzept:** GTD (Getting Things Done) unterscheidet ähnlich zwischen
Capture → Clarify → Organize → Reflect → Engage. Nicht 1:1 übertragbar, aber
die Grundidee (unterschiedliche mentale Modi brauchen unterschiedliche UI-Zustände)
ist dieselbe.

**Reifegrad:** Sehr frühe Idee — noch kein konkreter Vorschlag. Bevor hier etwas
umgebaut wird, braucht es einen klareren Entwurf: welche Modi gibt es genau,
was gehört jeweils dazu, und was fällt weg oder vereinfacht sich dadurch?
Wird gemeinsam weiterentwickelt.

---

## Mahlzeiten direkt auf der Mahlzeiten-Seite einplanen

**Kontext:** Aktuell muss man für jeden Tag einzeln in die Tages- oder Wochenansicht
navigieren, um eine Mahlzeit einzutragen. Auf der Mahlzeiten-Seite selbst (Ideen-Pool)
kann man Ideen verwalten, aber nicht direkt einem Tag zuweisen.

**Idee:** Auf der Mahlzeiten-Seite eine Wochenübersicht ergänzen — z.B. eine kompakte
7-Tage-Leiste — wo man Mahlzeiten per Tap oder Drag & Drop direkt einem Tag zuordnen kann.
Alternativ: beim Antippen einer Mahlzeiten-Idee ein "Einplanen"-Button, der einen
Datums-Picker öffnet.

**Varianten:**

| Variante | Beschreibung | Aufwand |
|----------|-------------|---------|
| A – Datums-Picker pro Idee | "Einplanen"-Button öffnet Datumsauswahl | Gering (~2h) |
| B – Wochenleiste auf Mahlzeiten-Seite | Kompakte 7-Tage-Ansicht, Ideen per Tap zuweisen | Mittel (~1 Tag) |
| C – Drag & Drop | Ideen auf Tages-Slots ziehen | Hoch, Touch-Support nötig |

**Empfehlung:** Variante A als schnellen Einstieg — löst den Kernschmerz
(zu viele Navigationsschritte) mit minimalem Aufwand.

---

## Redesign Heute-Ansicht — Abgrenzung zur Wochenansicht

**Kontext:** Die Heute- und die Wochenansicht überschneiden sich konzeptuell stark.
Heute zeigt einen einzelnen Tag, die Woche zeigt 7 Tage — aber das Gefühl und der Zweck
sind unterschiedlich: Heute = "Was tue ich jetzt?", Woche = "Was plane ich als nächstes?".
Aktuell ist diese Unterscheidung visuell nicht klar genug.

**Mögliche Richtungen (noch offen, kein Favorit):**

- **Heute als Dashboard:** Nicht nur Aufgabenliste, sondern ein echter Tagesüberblick —
  Termine, Mahlzeit, offene Aufgaben, ggf. Wetter oder Datum prominent. Eher eine
  "Schaltzentrale" als eine Liste.
- **Woche als primäre Planung, Heute als Filter:** Wochenansicht ist die Hauptansicht,
  Heute ist nur ein Highlight innerhalb davon (z.B. heutiger Tag hervorgehoben,
  Rest ausgegraut).
- **Heute fokussierter:** Weniger Elemente, mehr Ruhe — nur das Wesentliche für den
  aktuellen Tag, ohne Ablenkung durch den Rest der Woche.
- **Zusammenführung:** Heute und Woche als eine einzige Ansicht mit einem
  "Jetzt"-Fokus-Bereich oben und der restlichen Woche darunter.

**Reifegrad:** Noch unausgereift — erst konkreter Gestaltungsvorschlag (Mockup/Skizze)
bevor etwas implementiert wird.

---

## Kalender-Import (iCal / .ics)

**Kontext:** Viele externe Kalender (z.B. kommunale Abfallkalender, Schulferien, Feiertage)
bieten einen iCal-Export an. Diese Einträge sollen automatisch als Termine im Haushalt erscheinen,
ohne sie manuell anlegen zu müssen.

**Wie es funktioniert:** iCal (`.ics`) ist ein offener Standard — nahezu alle kommunalen
Abfallkalender unterstützen ihn. Backend parst die Datei und legt jeden Eintrag als
`FixedEvent` an. Library: `ical4j` (Kotlin/JVM, etabliert).

**Zwei Varianten:**

| Variante | Beschreibung | Aufwand |
|----------|-------------|---------|
| A – Datei-Upload | User lädt `.ics`-Datei einmalig hoch | Gering (~1 Tag) |
| B – Abo-URL | User gibt eine iCal-URL an, Backend lädt periodisch neu (Cron-Job) | Mittel (~2–3 Tage) |

**Offene Fragen:**
- Wiederholungsregeln aus iCal (`RRULE`) auf das interne `RecurrenceRule`-Modell mappen
  (nicht alle Regeln sind 1:1 übersetzbar)
- Bei Abo-URL: wie oft neu laden, was passiert bei Konflikten mit bereits geänderten Einträgen?
- Import-Scope: nur Termine (`VEVENT`) oder auch Aufgaben (`VTODO`)?

**Empfehlung:** Mit Variante A (Datei-Upload) starten — liefert sofort Mehrwert für den
Abfallkalender-Use-Case ohne Infrastruktur-Overhead.

---
