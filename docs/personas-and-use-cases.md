# Personas und Use Cases

## Personas

Es gibt genau eine Persona in zwei Ausprägungen:

### Haushaltsperson (A und B)

- Lebt in einem gemeinsamen Haushalt mit Partner:in
- Hat einen vollen Alltag (Beruf, Hobbys) und begrenzte mentale Kapazität für Haushaltsplanung
- Möchte nicht ständig nachfragen oder erinnern müssen, was ansteht
- Braucht keinen perfekten Plan, sondern Sichtbarkeit und geteilte Verantwortung

Beide Personen sind gleichberechtigt. Es gibt keine Admin-Rolle.

---

## Use Cases

### UC-1: Morgens den Tag überblicken

**Trigger:** Person öffnet die App morgens (mobil).
**Ziel:** In 10 Sekunden wissen, was heute ansteht.
**Was sichtbar ist:**
- Heutige Aufgaben (zugewiesene + unzugewiesene)
- Feste Termine (z.B. "Gelber Sack rausstellen")
- Essensplan für heute

### UC-2: Aufgabe spontan anlegen

**Trigger:** Einer fällt etwas auf ("Wir müssen den Wasserhahn reparieren lassen").
**Ziel:** In <15 Sekunden eine Aufgabe erfassen, optional mit Datum und Zuweisung.
**Ergebnis:** Aufgabe existiert im System, taucht zum richtigen Zeitpunkt auf.

### UC-3: Aufgabe erledigen

**Trigger:** Person hat etwas erledigt.
**Ziel:** Abhaken. Bei wiederkehrenden Aufgaben wird automatisch die nächste Instanz erzeugt.

### UC-4: Sonntagsplanung

**Trigger:** Paar setzt sich am Sonntag zusammen (10–15 Min).
**Ziel:** Gemeinsam die Woche vorbereiten.
**Ablauf:**
1. Wochenansicht öffnen – sehen, was schon terminiert ist
2. Offene Aufgaben sichten – ggf. auf Tage verteilen
3. Projektschritte prüfen – nächste Schritte für die Woche einplanen
4. Essensplan füllen – pro Tag eintragen, was gekocht wird

### UC-5: Projekt anlegen und vorantreiben

**Trigger:** Größeres Vorhaben steht an ("Keller aufräumen").
**Ziel:** Vorhaben in Teilschritte zerlegen, Schritte nach und nach einplanen.
**Ablauf:**
1. Projekt anlegen mit Titel und Ziel
2. Teilschritte definieren (flache Liste)
3. Einzelne Schritte bei Bedarf terminieren und/oder zuweisen
4. Fortschritt sehen (X von Y erledigt)

### UC-6: Wiederkehrende Aufgabe einrichten

**Trigger:** Etwas muss regelmäßig passieren ("Bad putzen", "Pflanzen gießen").
**Ziel:** Einmal einrichten, danach taucht es automatisch zum richtigen Zeitpunkt auf.
**Intervalle:** täglich, wöchentlich, 14-tägig, monatlich, bestimmter Wochentag.
