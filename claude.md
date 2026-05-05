# CLAUDE.md

## Projekt

Haushalts-App für einen Paarhaushalt (genau 2 Personen). Aufgaben-, Termin- und Projektorganisation mit Tages-/Wochenüberblick.

## Tech-Stack

- **Backend:** Kotlin, Hexagonal Architecture
- **Frontend:** SvelteKit (SPA/PWA, TypeScript)
- **Architektur:** Monorepo (`backend/` + `frontend/`), Domain-driven, fachlich sauber geschnitten

## Aktueller Stand

Iteration 1 implementiert: Task-CRUD (erstellen, Tagesansicht, abhaken) mit vollständiger Teststrategie.

## Wichtige Dokumente

- `docs/product-vision.md` – Produktvision und Designprinzipien
- `docs/personas-and-use-cases.md` – Personas und Use Cases
- `docs/mvp-scope.md` – MVP-Scope mit expliziter Abgrenzung
- `docs/domain-model.md` – Fachliches Domain Model
- `docs/information-architecture.md` – Screens, Flows, Navigation
- `docs/iteration-plan.md` – Iterationsplan in kleinen Schritten

## Leitprinzipien

- Schmaler MVP, hoher Nutzen
- Zusammenarbeit einfach halten (keine Rollen, keine Berechtigungen)
- Projekte = Aufgaben mit Struktur, nicht mehr
- Kein Over-Engineering: 1 Ebene Teilschritte, einfache Wiederholungslogik
- Mobile-first

## Konventionen

- Sprache in Code und Commits: Englisch
- Sprache in Produktdokumentation: Deutsch
- Domain-Begriffe: Household, Member, Task, Recurrence, Fixed Event, Project, Meal Note

## Commit-Format

```
<short imperative header: what changed>

<body: 2-3 sentences covering why this approach, key trade-offs, and what to watch out for>

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

Beispiel:
```
Add hexagonal architecture skeleton for backend

Ports and adapters keep the domain free of framework dependencies,
making business logic testable without Spring context. The trade-off
is more boilerplate compared to a layered architecture, which is
acceptable given the long-term maintainability goal.
```
