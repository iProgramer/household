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
