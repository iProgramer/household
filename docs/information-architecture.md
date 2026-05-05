# Information Architecture

## Hauptscreens

### 1. Heute (Home)
Der Einstiegsbildschirm. Beantwortet: "Was steht heute an?"

**Inhalte:**
- Heutige Aufgaben (zugewiesene zuerst, dann unzugewiesene)
- Heutige feste Termine
- Essensplan für heute
- Quick Action: Aufgabe abhaken, neue Aufgabe anlegen

### 2. Woche
Beantwortet: "Was kommt diese Woche?" – Hauptscreen für die Sonntagsplanung.

**Inhalte:**
- 7 Tage (Mo–So) mit jeweiligen Aufgaben, Terminen, Essensplan
- Bereich "Ungeplant": Offene Aufgaben ohne Datum (zum Einplanen)
- Essensplan pro Tag editierbar
- Aufgaben können ein Datum erhalten (auf Tag legen)

### 3. Projekte
Beantwortet: "Wie stehen unsere größeren Vorhaben?"

**Inhalte:**
- Liste aktiver Projekte mit Fortschrittsbalken
- Drill-down: Projektdetail mit Ziel + Teilschritte
- Teilschritte abhaken, terminieren, zuweisen
- Abgeschlossene Projekte ausblendbar

### 4. Alles (Backlog)
Beantwortet: "Was liegt alles an?"

**Inhalte:**
- Alle offenen Aufgaben, gruppiert: überfällig | heute | diese Woche | später | ungeplant
- Filterbar nach: meine / alle / unzugewiesen

---

## Kernobjekte und ihre Darstellung

| Objekt | Darstellung | Interaktionen |
|--------|-------------|---------------|
| Aufgabe | Checkbox + Titel + optionales Datum + Avatar | Abhaken, Datum setzen, zuweisen |
| Fester Termin | Icon + Titel (kein Checkbox) | Nur anzeigen |
| Projekt | Titel + Fortschrittsbalken | Öffnen → Teilschritte |
| Essensplan | Textfeld pro Tag | Inline editieren |

---

## Hauptflows

### Flow 1: Aufgabe erstellen
1. "+" Button (global verfügbar)
2. Titel eingeben (Pflicht)
3. Optional: Datum, Zuweisung, Projekt, Wiederholung
4. Speichern → Aufgabe erscheint im passenden Kontext

### Flow 2: Aufgabe erledigen
1. Checkbox antippen
2. Aufgabe wird als erledigt markiert
3. Bei Wiederholung: nächste Instanz wird erzeugt
4. Aufgabe verschwindet aus der Tagesansicht

### Flow 3: Woche planen (Sonntagsplanung)
1. Wochenansicht öffnen
2. Ungeplante Aufgaben sichten
3. Aufgaben auf Tage verteilen (Datum setzen)
4. Projektschritte prüfen, ggf. für diese Woche einplanen
5. Essensplan pro Tag ausfüllen

### Flow 4: Projekt anlegen
1. Projekte-Screen → "Neues Projekt"
2. Titel + Ziel eingeben
3. Teilschritte hinzufügen (Titel, optional Datum/Zuweisung)
4. Projekt ist aktiv und Schritte erscheinen im Tages-/Wochenkontext

---

## Navigation

```
[Heute] [Woche] [Projekte] [Alles]
         ↑ Bottom Navigation (mobil)
```

- 4 Hauptbereiche, direkt erreichbar
- "+" Button global sichtbar (Aufgabe schnell erfassen)
- Kein Hamburger-Menü, kein tiefes Nesting
