# Mesos — Progetto di Ingegneria del Software (AM25)

Implementazione del gioco da tavolo **Mesos** realizzata come progetto universitario per il corso di Ingegneria del Software al Politecnico di Milano (A.A. 2024–2025), gruppo **AM25**.

---

## Indice

- [Descrizione](#descrizione)
- [Requisiti](#requisiti)
- [Build](#build)
- [Esecuzione](#esecuzione)
- [Documentazione](#documentazione)

---

## Descrizione

Il progetto implementa una versione digitale di **Mesos** con architettura **Client/Server**:

- **Server** — gestisce la logica di gioco e le partite in corso
- **Client** — si connette al server e permette di giocare tramite:
  - **GUI** (interfaccia grafica JavaFX)
  - **TUI** (interfaccia testuale da terminale)

La comunicazione tra client e server avviene tramite due protocolli a scelta del client:
- **RMI** (porta `1099`)
- **Socket** (porta `6969`)

---

## Requisiti

- **Java 25** o superiore ([Eclipse Temurin](https://adoptium.net/) consigliato)
- **Maven** (in alternativa usare il wrapper `./mvnw` incluso nel progetto)

---

## Build

Per compilare il progetto e generare i JAR eseguibili:

```bash
./mvnw package -DskipTests
```

I JAR vengono prodotti nella cartella `target/`:

| File | Descrizione |
|------|-------------|
| `target/am25-Server-1.0-SNAPSHOT.jar` | Eseguibile del server |
| `target/am25-Client-1.0-SNAPSHOT.jar` | Eseguibile del client |

---

## Esecuzione

### 1. Avvio del Server

Avviare il server **prima** di qualsiasi client:

```bash
java -jar target/am25-Server-1.0-SNAPSHOT.jar
```

Il server si mette in ascolto sulla porta `1099` (RMI) e `6969` (Socket).

### 2. Avvio del Client

```bash
java -jar target/am25-Client-1.0-SNAPSHOT.jar
```

All'avvio il client chiederà di scegliere:
1. Il **protocollo di rete** (RMI o Socket)
2. L'**indirizzo IP** del server
3. La **modalità di gioco** (GUI o TUI)

> **Nota:** per giocare in locale, usare come indirizzo IP `localhost` o `127.0.0.1`.

---

## Documentazione

Tutta la documentazione tecnica del progetto si trova nella cartella [`Documentazione_AM25/`](Documentazione_AM25/).

| Cartella | Contenuto |
|----------|-----------|
| [`Documentazione_AM25/UML/`](Documentazione_AM25/UML/) | Diagrammi UML (class diagram, diagrammi di sequenza) |
| [`Documentazione_AM25/Design_Pattern/`](Documentazione_AM25/Design_Pattern/) | Report sui design pattern adottati (`Report Design AM25.pdf`) |
| [`Documentazione_AM25/Protocollo_di_Rete/`](Documentazione_AM25/Protocollo_di_Rete/) | Report sul protocollo di rete (`Report Protocollo di Rete.pdf`) |
