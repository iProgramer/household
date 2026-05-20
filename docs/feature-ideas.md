# Feature-Ideen (Backlog)

Kurze Notizen zu Ideen, die noch nicht umgesetzt sind. Kein Anspruch auf Vollständigkeit —
primär als Gedächtnisstütze für spätere Planung.

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

## Überfällige Aufgaben — Sichtbarkeit nicht erledigter Vortages-Tasks

**Kontext:** Offene Aufgaben mit einem Datum in der Vergangenheit tauchen weder in der
Heute-Ansicht noch in der Wochenansicht auf. Sie sind nicht verloren (in "Alles → Offen"
sichtbar), aber auch nicht aktiv präsent.

**Kernfrage (noch offen):** Sollen überfällige Aufgaben automatisch auf heute verschoben
werden, oder sollen sie als "Rückstand" explizit angezeigt werden?

**Varianten:**

| Variante | Beschreibung | Vorteil | Nachteil |
|----------|-------------|---------|----------|
| A – Abschnitt "Überfällig" in Heute | Eigener roter Bereich oben, Query `date < today AND status = OPEN` | Ursprungsdatum bleibt, bewusste Entscheidung nötig | Kann sich anhäufen |
| B – Auto-Rollover auf heute | Beim Laden alle überfälligen Tasks auf `today` datieren | Immer präsent in Heute-Ansicht | Datumsverlust, fühlt sich heimlich an |
| C – Badge in der Navigation | Rote Zahl auf dem Heute-Icon | Dezent, kein Platzbedarf | Man muss trotzdem aktiv handeln |

**Backend-Aufwand:** Gering — neuer Endpoint `GET /api/tasks/overdue` (`date < today AND status = OPEN`).
Frontend-Aufwand: A ~2h, B ~1h, C ~1h.

**Tendenz:** Aufgaben sollen sichtbar bleiben (kein stilles Verschwinden),
konkrete Darstellung noch offen.

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
