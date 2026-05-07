# Deployment Guide

Dieser Guide beschreibt das Einrichten der App auf einem Hetzner-Server
von Null bis zur laufenden HTTPS-Instanz.

---

## Voraussetzungen

- Hetzner-Account (cloud.hetzner.com)
- Eine Domain, deren DNS du verwalten kannst
- Git-Repository der App (GitHub, GitLab o.ä.)

---

## 1. Server anlegen (Hetzner Cloud)

1. Neues Projekt anlegen → **Server erstellen**
2. Einstellungen:
   - **Image:** Ubuntu 24.04
   - **Typ:** CX22 (2 vCPU, 4 GB RAM, ~4 €/Monat)
   - **Region:** Falkenstein oder Nürnberg (DE)
   - **SSH-Key:** deinen öffentlichen Key hinterlegen (`~/.ssh/id_ed25519.pub`)
3. Server erstellen – du bekommst eine IPv4-Adresse (z.B. `1.2.3.4`)

---

## 2. DNS einrichten

Bei deinem Domain-Anbieter einen A-Record anlegen:

```
haushalt.deinedomain.de  →  A  →  1.2.3.4
```

> DNS-Änderungen brauchen manchmal 5–30 Minuten bis sie weltweit aktiv sind.

---

## 3. Server einrichten

```bash
# Per SSH einloggen
ssh root@1.2.3.4

# System aktualisieren
apt update && apt upgrade -y

# Docker installieren (offizielles Script)
curl -fsSL https://get.docker.com | sh

# Prüfen ob Docker läuft
docker --version
docker compose version
```

---

## 4. App deployen

```bash
# App-Verzeichnis anlegen
mkdir -p /opt/household
cd /opt/household

# Repository klonen
git clone https://github.com/dein-user/todo_app.git .

# .env aus Vorlage erstellen
cp .env.example .env
```

Jetzt `.env` mit echten Werten befüllen:

```bash
nano .env
```

```dotenv
APP_DOMAIN=haushalt.deinedomain.de

DB_NAME=household
DB_USER=household
DB_PASSWORD=<langes-zufälliges-passwort>

JWT_SECRET=<mind-32-zufällige-zeichen>
```

Zufällige Werte generieren:
```bash
# Für DB_PASSWORD und JWT_SECRET je einmal ausführen:
openssl rand -base64 32
```

Stack starten:
```bash
docker compose up -d
```

Beim ersten Start:
- Docker baut die Images (~2–3 Minuten)
- Caddy holt automatisch ein SSL-Zertifikat von Let's Encrypt
- Die App ist danach unter `https://haushalt.deinedomain.de` erreichbar

---

## 5. Status prüfen

```bash
# Alle Container laufen?
docker compose ps

# Backend-Logs (Startfehler sichtbar)
docker compose logs backend

# Caddy-Logs (Zertifikat-Status)
docker compose logs caddy
```

---

## 6. Updates einspielen

```bash
cd /opt/household

# Neue Version holen
git pull

# Images neu bauen und Container ersetzen
docker compose up -d --build
```

Laufende Nutzer werden kurz unterbrochen (~10–30 Sekunden), die Daten
bleiben erhalten (PostgreSQL-Volume bleibt bestehen).

---

## 7. Backup

Die PostgreSQL-Daten liegen in einem Docker Volume. Backup erstellen:

```bash
docker compose exec db pg_dump -U household household > backup_$(date +%Y%m%d).sql
```

Wiederherstellen:
```bash
cat backup_20260101.sql | docker compose exec -T db psql -U household household
```

---

## Troubleshooting

**Backend startet nicht:**
```bash
docker compose logs backend
```
Häufige Ursache: falsche DB-Credentials in `.env` oder Datenbank noch nicht bereit.

**Kein SSL-Zertifikat:**
Caddy braucht eine öffentlich erreichbare Domain. Prüfen ob der DNS-A-Record
auf die richtige IP zeigt:
```bash
dig haushalt.deinedomain.de
```

**Daten nach Update weg:**
Nur wenn `docker compose down -v` verwendet wurde (löscht Volumes).
Immer ohne `-v` arbeiten: `docker compose down`.
